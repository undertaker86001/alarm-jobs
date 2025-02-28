package com.sucheon.alarm.functions;

import com.github.jesse.l2cache.Cache;
import com.github.jesse.l2cache.CacheConfig;
import com.sucheon.alarm.constant.CacheConstant;
import com.sucheon.alarm.event.RuleMatchResult;
import com.sucheon.alarm.event.alarm.AlarmContext;
import com.sucheon.alarm.event.alarm.AlarmLevelMatchWatermark;
import com.sucheon.alarm.event.alarm.AlarmMatchCacheInterval;
import com.sucheon.alarm.event.alarm.AlarmMatchResult;
import com.sucheon.alarm.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.sucheon.alarm.constant.StateConstant.*;
import static com.sucheon.alarm.utils.AssembleDataUtil.fetchCacheState;
import static com.sucheon.alarm.utils.TransferBeanutils.accessCurrentCacheInstance;

/**
 * 告警超过过线计数 比如配置容忍度 ,恶化通知, 报警间隔等一系列报警指标
 */
@Slf4j
public class AlarmOverheadCountFunction extends ProcessFunction<RuleMatchResult, AlarmMatchResult> implements CheckpointedFunction {

    /**
     * 定义缓存配置类
     */
    private CacheConfig cacheConfig;


    private Map<String, String> metadataMapping;

    /**
     * 缓存的flink上下文状态(每个告警等级是否触发过线计数需要更新的state)
     */
    private transient ListState<AlarmLevelMatchWatermark> alarmLevelState;


    /**
     * 待提交的部分数据(每个告警等级是否触发过线计数需要更新的state)
     */
    private List<AlarmLevelMatchWatermark> alarmLevelWatermarkList;


    /**
     * 缓存的flink上下文状态(缓存正常计数)
     */
    private transient ListState<Integer> currentObservedNormalCountState;

    /**
     * 待提交的部分数据(缓存正常计数)
     */
    private List<Integer> currentObservedNormalCountList;

    /**
     * 缓存的设备时间戳状态(缓存设备时间戳)
     */
    private transient ListState<Long> currentDeviceTimestampState;


    /**
     * 当前设备时间戳列表(缓存当前设备时间戳，只会有一位)
     */
    private transient List<Long> currentDeviceTimestampList;


    /**
     * 报警升级维护标识
     */
    private List<String> currentAlarmLevelUpgradeSymbolList;

    /**
     *
     */
    private transient ListState<String> currentAlarmLevelUpgradeSymbolState;


    /**
     * <告警等级， 超过告警等级的水位线上下文信息>
     */
    private ConcurrentHashMap<String, AlarmLevelMatchWatermark> overheadAlarmWatermark;


    /**
     * <告警等级, 该告警等级过线次数>
     */
    private ConcurrentHashMap<String, AtomicInteger> overheadAlarmCount;



    /**
     * 告警等级升级标识
     */
    private boolean alarmLevelUpGradeSymbol;


    /**
     * 上一次命中的告警等级
     */
    private String prevAlarmLevel;

    /**
     * 上一次计数的时间戳(用于统计数据时间间隔)
     */
    private Long prevDataTime;

    /**
     * 写入kafka的目标端
     */
    private String sinkTopic;

    /**
     * 攒批做快照的阈值
     */
    private Integer threshold;

    public AlarmOverheadCountFunction(CacheConfig cacheConfig, String sinkTopic, Integer threshold){
        this.cacheConfig = cacheConfig;
        this.metadataMapping = MetadataUtils.assembleChannelAndCacheInstance();
        this.sinkTopic = sinkTopic;
        this.threshold = threshold;
        this.overheadAlarmCount = new ConcurrentHashMap<>();
        this.overheadAlarmWatermark = new ConcurrentHashMap<>();
        this.alarmLevelUpGradeSymbol = false;
        this.prevAlarmLevel = "";
        this.prevDataTime = 0L;
    }

    @Override
    public void open(Configuration parameters) {
        alarmLevelWatermarkList = new ArrayList<>();
        currentObservedNormalCountList = new ArrayList<>();
    }

    @Override
    public void processElement(RuleMatchResult currentAlarmOverHeadData, ProcessFunction<RuleMatchResult, AlarmMatchResult>.Context context, Collector<AlarmMatchResult> collector) {
        Map<String, Object> pointData = new HashMap<>();

        //获取当前命中事件告警等级
        String currentAlarmLevel = currentAlarmOverHeadData.getAlarmLevel();

        Long currentDeviceTimestamp = currentAlarmOverHeadData.getDeviceTimestamp();


        //维护正常计数变量到状态当中
        Integer currentNormalCount = currentObservedNormalCountList.get(0);
        AtomicInteger normalCount = new AtomicInteger(currentNormalCount);
        normalCount.getAndIncrement();

        //是否距离上一笔数据超过一周
        Long prevDeviceTimestamp =  currentDeviceTimestampList.get(0);
        //todo 超过则重置计数
        if (currentDeviceTimestamp - prevDeviceTimestamp > 7){
            //清空正常计数
            normalCount.set(0);
            //清空过线计数，报警间隔
            alarmLevelState.clear();
        }

        //清空待提交的缓存计数器
        currentObservedNormalCountList.clear();
        currentObservedNormalCountList.add(normalCount.get());


        //todo 通过枚举转换状态 设置当前告警等级
        Integer currentAlarmLevelValue = Integer.valueOf(currentAlarmLevel);

        if (StringUtils.isBlank(prevAlarmLevel)){
            prevAlarmLevel = currentAlarmLevel;
        }
        Integer prevAlarmLevelValue = Integer.valueOf(prevAlarmLevel);

        //如果告警等级不一样，判断是否比前一个告警等级要高, 如果更高则升级
        //todo 等级标识可能需要通过算子状态checkpoint保证状态恢复
        if (!prevAlarmLevel.equals(currentAlarmLevel)){
            if (prevAlarmLevelValue < currentAlarmLevelValue){
                alarmLevelUpGradeSymbol = true;
            }else {
                alarmLevelUpGradeSymbol = false;
            }
        }else {
            alarmLevelUpGradeSymbol = false;
        }


        //比较上一条数据是否是关机前的最后一条数据, 如果数据间隔超过一秒，
        // 则需要重置时间戳

        //todo 实现数据时间间隔转换
        String dataInterval = "";

        Long dataIntervalValue = Long.valueOf(dataInterval);

        boolean isCloseMachine = false;
        if (prevDataTime == 0L){
            prevDataTime = currentDeviceTimestamp;
        } else if (currentAlarmLevelValue - prevDataTime == 1){
            prevDataTime = currentDeviceTimestamp;
        }else {
            //如果上一条数据比当前数据小一秒以上 说明关机了
            isCloseMachine = true;
        }

        long dataIntervalDiff = currentDeviceTimestamp - prevDataTime;

        // 是否命中数据时间间隔条件
        boolean isDataIntervalMatch =false;
        if (isCloseMachine && dataIntervalValue > dataIntervalDiff ){
            isDataIntervalMatch = true;
        }


        //当前事件归属于哪个告警等级的过线计数
        AtomicInteger currentLevelOverheadCount = overheadAlarmCount.get(currentAlarmLevel);


        //如果哪个告警等级过线了，则过线计数
        boolean isOverHeadCount = currentAlarmOverHeadData.getOverHeadCount();
        if (isOverHeadCount) {
            if (currentLevelOverheadCount == null) {
                currentLevelOverheadCount = new AtomicInteger(0);
            } else {
                currentLevelOverheadCount.getAndIncrement();
            }
            overheadAlarmCount.put(currentAlarmLevel, currentLevelOverheadCount);
        }else {

        }

        try {
            // 计算出等待时间间隔  每一秒代表一条数据发送
            // 从redis当中取拉报警间隔期间的数据 比较出最大告警峰值是否在告警间隔内命中
            AlarmContext alarmContext = new AlarmContext();
            BeanUtils.copyProperties(alarmContext, currentAlarmOverHeadData);

            //比较告警等级集合和当前命中数据的告警结果是否匹配，如果匹配的话取出对应告警等级中老的记录
            Iterable<AlarmLevelMatchWatermark> ruleMatchResults =  alarmLevelState.get();
            AtomicReference<AlarmLevelMatchWatermark> matchAlarmData = new AtomicReference<>(new AlarmLevelMatchWatermark());
            List<AlarmLevelMatchWatermark> historyCacheAlarmLevelWatermark = new ArrayList<>();

            //如果状态清空了，重新实例化避免空指针
            if (ruleMatchResults == null){
                ruleMatchResults = new ArrayList<>();
            }

            ruleMatchResults.forEach( x-> {
                if (!InternalTypeUtils.iaAllFieldsNull(x) && currentAlarmLevel.equals(x.getAlarmLevel())){
                    matchAlarmData.set(x);
                }
                historyCacheAlarmLevelWatermark.add(x);
            } );

            //计算出第一次过线和目前最近一次过线的数据
            AlarmMatchCacheInterval cacheInterval = MergeAlarmDataUtils.alarmMatchCacheInterval(historyCacheAlarmLevelWatermark);


            //todo 解析数据正常计数
            String observedNormalCount = "";


            int observedNormalCountValue = Integer.parseInt(observedNormalCount);
            // 如果是比当前最后一条告警的时间戳最新，或推送的消息是正常计数
            // 则重置计数
            if (isDataIntervalMatch ||  ( currentDeviceTimestamp - cacheInterval.getLastAlarmTime() == observedNormalCountValue) && !currentAlarmOverHeadData.getOverHeadCount()){
                normalCount.set(0);
            }else {
                normalCount.getAndIncrement();
            }
            //更新缓存变量
            currentObservedNormalCountList.add(normalCount.get());



            //比较新数据和旧数据判断逻辑
            AlarmLevelMatchWatermark oldRecord = matchAlarmData.get();

            //如果清空过线计数之后，需要重新初始化过线计数
            if (InternalTypeUtils.iaAllFieldsNull(oldRecord)){
                oldRecord.setAlarmLevelRecordHistoryHighValue(new HashMap<>());
            }else {
                oldRecord.setAlarmLevel(currentAlarmOverHeadData.getAlarmLevel());
            }



            //解析出当前告警等级的数据, 与状态中的旧数据做对比
            AlarmLevelMatchWatermark currentRecord = MergeAlarmDataUtils.updateAlarmTimeByLevel(oldRecord, currentAlarmOverHeadData);
            //记录每个告警等级最新的命中瞬时时间，和过报警线最近的一次报警间隔
            AlarmLevelMatchWatermark currentUpdateRecord = MergeAlarmDataUtils.mergeData(currentRecord, currentAlarmOverHeadData);


            //维护各个告警等级的水位线和携带信息
            if (overheadAlarmWatermark.size() == 0){
                overheadAlarmWatermark.put(alarmContext.getAlarmLevel(), currentUpdateRecord);
            }

            //用全局变量缓存，保证线程安全
            //维护各个告警等级对应的历史最高报警数据 记录checkpoint 记录发生错误的快照
            alarmLevelWatermarkList.forEach(x -> {
                String curAlarmLevel =  x.getAlarmLevel();
                AlarmLevelMatchWatermark updateAlarmWaterMarkByLevel = overheadAlarmWatermark.get(curAlarmLevel);
                if (updateAlarmWaterMarkByLevel != null) {
                    alarmLevelWatermarkList.remove(x);
                    alarmLevelWatermarkList.add(updateAlarmWaterMarkByLevel);
                } });

            if (alarmLevelWatermarkList.size() >= threshold){
                alarmLevelWatermarkList.clear();
            }



            //做工况报警, 不分工况需要考虑容忍度和恶化通知的依赖关系

            //预计算出所有告警等级的报警间隔
            Map<String, Long> alarmIntervalByLevel = new HashMap<>();

            overheadAlarmWatermark.forEach((level, alarmLevelWatermark) -> {
                Long alarmTimeDiff = alarmLevelWatermark.getCurrentMatchAlarmTime() - alarmLevelWatermark.getFirstMatchAlarmTime();
                alarmIntervalByLevel.put(level, alarmTimeDiff);
            });

            //todo 解析出报警间隔, 默认300
            String alarmInterval = "300";
            //todo 解析出恶化通知 默认开还是关
            String worsenNotice = "true";
            //todo 解析出容忍度 连续超过任意阈值线多少次不会报警
            String tolerance = "3";

            Integer toleranceValue = Integer.valueOf(tolerance);


            //todo 填充需要发送到下游的告警数据
            AlarmMatchResult alarmMatchResult = new AlarmMatchResult();
            alarmMatchResult.setTopic(sinkTopic);
            alarmMatchResult.setAlarmId(currentAlarmOverHeadData.getAlarmId());

            //按照告警等级，从低到高排序
            overheadAlarmWatermark.entrySet().stream().sorted(CompareUtils::compareAlarmLevel);


            //过线计数 计算截止到本次有多少次报警
            int countUtilCurrent = 0;
            for (Map.Entry<String, AtomicInteger> alarmItem: overheadAlarmCount.entrySet()){

                String alarmLevelKey = alarmItem.getKey();
                Integer alarmLevel = Integer.parseInt(alarmLevelKey);
                AtomicInteger currentCountOverLine = alarmItem.getValue();
                countUtilCurrent += currentCountOverLine.get();
                if (alarmLevel > currentAlarmLevelValue){
                    break;
                }
            }

            //如果过线计数大于0，且存在告警等级的升级标识
            if (countUtilCurrent >0 && alarmLevelUpGradeSymbol){
                //判断是否在报警间隔内触发更高等级报警则推送
                for (Map.Entry<String, Long> item: alarmIntervalByLevel.entrySet()){
                    Long alarmIntervalValue = Long.valueOf(alarmInterval);
                    if (!alarmIntervalValue.equals(item.getValue())){
                        continue;
                    }
                    boolean worsenNoticeSymbol = Boolean.parseBoolean(worsenNotice);
                    //如果开启恶化通知, 过线计数是否超过容忍度范围
                    boolean isOpenWorsen = worsenNoticeSymbol && countUtilCurrent > toleranceValue;
                    if (!isOpenWorsen){
                        continue;
                    }
                    collector.collect(alarmMatchResult);
                }
            }


            //缓存最高历史峰值
            if (oldRecord.isUpdate()) {
                Cache cacheHightHistoryValue = accessCurrentCacheInstance(CacheConstant.ALARM_RULE_DATA_CACHE_INSTANCE, cacheConfig);
                cacheHightHistoryValue.put(CacheConstant.ALARM_RULE_DATA_CACHE_INSTANCE, oldRecord);
            }

            collector.collect(alarmMatchResult);
        }catch (Exception ex){
            String errorMessage = ExceptionUtil.getErrorMessage(ex);
            log.error("【事件处理失败】原始数据为: {}, 故障原因为:{}", pointData, errorMessage);
        }
    }


    @Override
    public void snapshotState(FunctionSnapshotContext snapshotContext) throws Exception {
        alarmLevelState.clear();
        alarmLevelState.update(alarmLevelWatermarkList);

        currentObservedNormalCountState.clear();
        currentObservedNormalCountState.update(currentObservedNormalCountList);

        currentObservedNormalCountState.clear();
        currentDeviceTimestampState.update(currentDeviceTimestampList);



    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        //初始化需要保存的状态


        //恢复需要进行每个告警等级历史最高峰值
        long startTime = System.currentTimeMillis();
        alarmLevelState = context.getOperatorStateStore().getListState(alarmLevelDescriptor);
        if (context.isRestored()) {
            for (AlarmLevelMatchWatermark element : alarmLevelState.get()) {
                alarmLevelWatermarkList.add(element);
                overheadAlarmWatermark.put(element.getAlarmLevel(), element);
            }
            log.info("各个告警等级对应的State初始化恢复数据成功:{}|{}ms", alarmLevelWatermarkList.size(), System.currentTimeMillis() - startTime);
        }else {

        }

        //恢复需要进行正常计数的统计值
        currentObservedNormalCountList = fetchCacheState("observedNormalCount",context, observedNormalCountDescriptor);
        currentObservedNormalCountState = AssembleDataUtil.fetchCurrentState(context, observedNormalCountDescriptor);

        //恢复需要进行记录设备当前时间戳
        currentDeviceTimestampList = fetchCacheState("currentDeviceTimestamp", context, currentDeviceTimestampDescriptor);
        currentDeviceTimestampState = AssembleDataUtil.fetchCurrentState(context, currentDeviceTimestampDescriptor);

        currentAlarmLevelUpgradeSymbolList = fetchCacheState("prev-alarm-symbol", context, currentPrevAlarmSymbolDescriptor);
        currentAlarmLevelUpgradeSymbolState = AssembleDataUtil.fetchCurrentState(context, currentPrevAlarmSymbolDescriptor);


    }


}
