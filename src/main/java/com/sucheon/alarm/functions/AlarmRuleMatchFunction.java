package com.sucheon.alarm.functions;

import cn.hutool.core.util.IdUtil;
import com.github.jesse.l2cache.Cache;
import com.github.jesse.l2cache.CacheConfig;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.sucheon.alarm.constant.CacheConstant;
import com.sucheon.alarm.constant.CommonConstant;
import com.sucheon.alarm.constant.OutputTagConstant;
import com.sucheon.alarm.event.*;
import com.sucheon.alarm.config.HttpConf;
import com.sucheon.alarm.event.alarm.AlarmMetric;
import com.sucheon.alarm.event.alarm.TemporaryAlarm;
import com.sucheon.alarm.event.oc.OcAlarmRuleMapping;
import com.sucheon.alarm.event.oc.OcChangeObject;
import com.sucheon.alarm.event.pointtree.PointOriginData;
import com.sucheon.alarm.event.pointtree.PointTreeSpace;
import com.sucheon.alarm.event.rule.AlarmExtCondition;
import com.sucheon.alarm.event.rule.AlarmLevelAndTreeSpaceMapping;
import com.sucheon.alarm.event.rule.AlarmRuleMapping;
import com.sucheon.alarm.singleton.*;
import com.sucheon.alarm.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;

import java.util.*;
import java.util.stream.Collectors;

import static com.sucheon.alarm.customenum.MetricPriorityEnum.DATA_COLUMN;
import static com.sucheon.alarm.customenum.MetricPriorityEnum.OC_INSTANCE;

/**
 * 处理规则计算逻辑
 */
@Slf4j
public class AlarmRuleMatchFunction extends BroadcastProcessFunction<String, String, RuleMatchResult> {

    /**
     * 配置的缓存配置
     */
    private CacheConfig cacheConfig;

    /**
     * redis客户端
     */
    private RedissonClient redissonClient;

    /**
     * 外网部署配置
     */
    private HttpConf httpConf;

    public AlarmRuleMatchFunction(CacheConfig cacheConfig, HttpConf httpConf){
        this.cacheConfig = cacheConfig;
        this.httpConf = httpConf;
    }

    @Override
    public void open(Configuration parameters) {
        this.redissonClient = Redisson.create(cacheConfig.getRedis().getRedissonConfig());
    }

    @Override
    public void processElement(String pointDataStr, BroadcastProcessFunction<String, String, RuleMatchResult>.ReadOnlyContext readOnlyContext, Collector<RuleMatchResult> collector){
        try {



            ExpressRunner runner = ExpressRunnerSingleton.INSTANCE.getInstance();

            //记录工况表达式求值的原始数据
            DistributeData distributeData = CommonConstant.objectMapper.readValue(pointDataStr, DistributeData.class);
            DefaultContext<String, Object> context = AssembleDataUtil.assemblePreMatchData(distributeData);

            Object pointId = context.get("point_id");
            Object algInstanceId = context.get("alg_instance_id");

            if (pointId == null && algInstanceId == null){
                log.error("当前上送的数据异常， 请重新解析！ 原始串为: {}", pointDataStr);
                return;
            }


            OcChangeObject ocChangeObject = new OcChangeObject();
            Map<String, AlarmMetric> alarmLevelSettings = new HashMap<>();

            //根据点位树空间匹配对应上送的测点数据

            List<PointTreeSpace> pointTreeSpaceList = TransferBeanutils.transferDataByList(log, CacheConstant.TERMINAL_POINT_TREE, PointTreeSpaceSingleton.INSTANCE.getInstance(), PointTreeSpace.class, cacheConfig);

            PointOriginData currentPointData = new PointOriginData();
            currentPointData.setPointId(String.valueOf(pointId));
            List<PointTreeSpace> afterFilterDataList =  pointTreeSpaceList.stream().filter(x -> {
                List<PointOriginData> pointOriginDataList =  x.getPointIds();
                return pointOriginDataList.contains(currentPointData);

            }).collect(Collectors.toList());

            PointTreeSpace pointTreeSpace = afterFilterDataList.get(0);
            String currentBelongToSpaceId = pointTreeSpace.getSpaceId();


            //组装成告警等级当前表达式

            List<AlarmDataSourceObject> alarmDataSourceObjectList = TransferBeanutils.transferDataByList(log, CacheConstant.ALARM_RULE_DATA_CACHE_INSTANCE, new AlarmDataSourceObject(), AlarmDataSourceObject.class, cacheConfig);

            if (alarmDataSourceObjectList == null) {
                log.error("当前缓存中还没更新值, 暂时无法匹配到各个告警等级的表达式!");
                return;
            }


            //<点位树空间, 告警等级表达式> 的映射关系
            Map<String, AlarmLevelAndTreeSpaceMapping> alarmLevelAndTreeSpaceMappingMap = new HashMap<>();
            AlarmLevelAndTreeSpaceMapping alarmLevelAndTreeSpaceMapping = new AlarmLevelAndTreeSpaceMapping();
            alarmLevelAndTreeSpaceMapping.setSpaceId(currentBelongToSpaceId);
            alarmDataSourceObjectList = alarmDataSourceObjectList.stream().filter(alarmDataSourceObject ->  {
                List<AlarmLevelAndTreeSpaceMapping> alarmLevelAndTreeSpaceMappingList =  alarmDataSourceObject.getAlarmLevelAndTreeSpaceMapping();
                alarmLevelAndTreeSpaceMappingList.forEach(x -> { alarmLevelAndTreeSpaceMappingMap.put(x.getSpaceId(), x); });
                return alarmLevelAndTreeSpaceMappingList.contains(alarmLevelAndTreeSpaceMapping);

            }).collect(Collectors.toList());

            AlarmDataSourceObject filterAlarmDataSourceObject = alarmDataSourceObjectList.get(0);

            AlarmLevelAndTreeSpaceMapping currentAlarmLevelExpressionList = alarmLevelAndTreeSpaceMappingMap.get(currentBelongToSpaceId);



            //todo 如果需要匹配算法实例id 从分发侧匹配完之后 应该带着spaceId和tree_node_id带下来

            //todo 匹配哪些工况表达式需要调用工况表达式接口匹配 (需要通过算法配置服务传过来的数据树节点编号，算法实例以及对应的工况表达式做匹配)
            List<AlarmRuleMapping> alarmRuleMappingList = filterAlarmDataSourceObject.getAlarmRuleMapping();
            List<OcAlarmRuleMapping> ocAlarmRuleMappingList = TransferBeanutils.transferDataByList(log, CacheConstant.ALARM_OC_DATA_CACHE_INSTANCE,new OcAlarmRuleMapping(), OcAlarmRuleMapping.class, cacheConfig);



            //工况如果此时被删除，则表达式匹配不到
            AlarmRuleMapping current = AlarmRuleMappingSingleton.INSTANCE.getInstance();
            for (AlarmRuleMapping ruleItem: alarmRuleMappingList){
                String ocAlarmRuleInstanceId = ruleItem.getOcInstanceId();
                for (OcAlarmRuleMapping ocAlarmRuleMapping: ocAlarmRuleMappingList) {
                    String ocAlarmInstanceId = ocAlarmRuleMapping.getOcInstanceId();
                    if (ocAlarmInstanceId.equals(ocAlarmRuleInstanceId)){
                        if (ocAlarmRuleMappingList.contains(ruleItem)){
                            current = ruleItem;
                            break;
                        }
                    }
                }
            }



            if (current == null || InternalTypeUtils.iaAllFieldsNull(current)) {
                log.error("当前缓存中还没更新值, 暂时无法匹配到工况!");

                //需要通过http接口做重试, 存放到缓存当中
                return;
            } else {

                //解析告警配置
                ocChangeObject.setAlarmLevelList(currentAlarmLevelExpressionList.getAlarmLevelList());
                ocChangeObject.setSpaceId(currentAlarmLevelExpressionList.getSpaceId());
                AlarmExtCondition alarmExtCondition = alarmLevelAndTreeSpaceMapping.getExtCondition();
                ocChangeObject.setExtCondition(alarmExtCondition);

                //尝试将当前点位树空间下的测点放入缓存， 重试几次
                alarmLevelSettings = RetryUtils.executeWithRetry( () -> AlarmLevelSettingUtils.transferAlarmLevel(ocChangeObject), 3, 2000, false);
            }


            //<当前告警等级,待计算的指标>
            Map<String, AlarmMetric> ocInstanceMetricList = alarmLevelSettings.entrySet().stream().filter( (k) -> {
                int priority = k.getValue().getMetricPriority();
                if (priority == OC_INSTANCE.getPriority()){
                    return true;
                }
                return false;
            } ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // <工况指标, 计算结果>
            List<String> ocInstanceComputeResult = new ArrayList<>();

            //工况指标计算
            for (Map.Entry<String, AlarmMetric> entry: ocInstanceMetricList.entrySet()){

                AlarmMetric alarmMetric =  entry.getValue();
                String metricExpression = alarmMetric.getMetricExpression();

                Object result = runner.execute(metricExpression, context, null, true, false);
                boolean result_symbol = Boolean.parseBoolean(String.valueOf(result));
                if (result_symbol) {
                    ocInstanceComputeResult.add(entry.getKey());
                }
            }


            Map<String, AlarmMetric> dataColumnMetricList = alarmLevelSettings.entrySet().stream().filter( (k) -> {
                int priority = k.getValue().getMetricPriority();
                if (priority == DATA_COLUMN.getPriority()){
                    return true;
                }
                return false;
            } ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));



            //数据字段指标计算 <告警等级, 命中计算结果>
            Map<String, Boolean> alarmLevelReportByMutliOcInstance = new HashMap<>();
            for (Map.Entry<String, AlarmMetric> dataColumnMetric: dataColumnMetricList.entrySet()){
                AlarmMetric alarmMetric = dataColumnMetric.getValue();
                String expression  = alarmMetric.getMetricExpression();
                String alarmLevel = dataColumnMetric.getKey();

                boolean matchResultSymbol = ExpressionUtils.computeOcExpression(expression, context, ocInstanceComputeResult);

                if (matchResultSymbol) {
                    alarmLevelReportByMutliOcInstance.put(alarmLevel, matchResultSymbol);
                }

            }

            String alarmId = IdUtil.getSnowflakeNextIdStr();
            //根据<告警等级,命中结果> 判断是否要进行下一级告警
            for (Map.Entry<String, Boolean> item: alarmLevelReportByMutliOcInstance.entrySet()){
                boolean matchSuccess = item.getValue();
                String alarmLevel = item.getKey();
                RuleMatchResult ruleMatchResult = RuleMatchResultSingleton.INSTANCE.getInstance();
                BeanUtils.copyProperties(ruleMatchResult, distributeData);
                ruleMatchResult.setAlarmTime(String.valueOf(System.currentTimeMillis()));
                ruleMatchResult.setAlarmLevel(alarmLevel);

                if (matchSuccess) {
                    ruleMatchResult.setAlarmId(alarmId);
                    ruleMatchResult.setOverHeadCount(true);
                }else {
                    ruleMatchResult.setAlarmId("");
                    ruleMatchResult.setOverHeadCount(false);
                }
                //投递到临时告警链路
                TemporaryAlarm temporaryAlarm = TemporaryAlarmSingleton.INSTANCE.getInstance();
                BeanUtils.copyProperties(temporaryAlarm, ruleMatchResult);
                readOnlyContext.output(OutputTagConstant.TEMPORARY_ALARM, temporaryAlarm);
                //投递到过线计数函数(数据标定链路)
                collector.collect(ruleMatchResult);
            }

        }catch (Exception ex){
            String errorMessage = ExceptionUtil.getErrorMessage(ex);
            log.error("当前工况配置失败, 失败原因为: {}", errorMessage);
        }

    }

    @Override
    public void processBroadcastElement(String pointDataStr, BroadcastProcessFunction<String, String, RuleMatchResult>.Context context, Collector<RuleMatchResult> collector) throws Exception {

    }
}
