package com.sucheon.alarm.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.jesse.l2cache.Cache;
import com.github.jesse.l2cache.CacheConfig;
import com.ql.util.express.DefaultContext;
import com.sucheon.alarm.constant.CacheConstant;
import com.sucheon.alarm.constant.CommonConstant;
import com.sucheon.alarm.event.AlarmDataSourceObject;
import com.sucheon.alarm.event.DistributeData;
import com.sucheon.alarm.event.RuleMatchResult;
import com.sucheon.alarm.event.pointtree.PointOriginData;
import com.sucheon.alarm.event.pointtree.PointTreeSpace;
import com.sucheon.alarm.event.rule.AlarmLevelAndTreeSpaceMapping;
import com.sucheon.alarm.singleton.PointTreeSpaceSingleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.checkerframework.checker.units.qual.A;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class AssembleDataUtil {


    /**
     * 将上游传递下来的过线数据拍平
     * @param currentAlarmOverHeadData
     * @return
     */
    public static Map<String, Object> assembleOverheadData(RuleMatchResult currentAlarmOverHeadData) {

        Map<String, Object> pointData = new HashMap<>();
        try {
            String iotJson = currentAlarmOverHeadData.getIotJson();
            Map<String, Object> iotColumnValue = CommonConstant.objectMapper.readValue(iotJson, new TypeReference<Map<String, Object>>() {
            });
            iotColumnValue.entrySet().stream().map(x -> {
                        pointData.put(x.getKey(), String.valueOf(Optional.ofNullable(x.getValue()).orElse("-1L")));
                        return x;
                    }
            ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            String algJson = currentAlarmOverHeadData.getAlgJson();
            Map<String, Object> algColumnValue = CommonConstant.objectMapper.readValue(algJson, new TypeReference<Map<String, Object>>() {
            });
            algColumnValue.entrySet().stream().map(x -> {
                        pointData.put(x.getKey(), String.valueOf(Optional.ofNullable(x.getValue()).orElse("-1L")));
                        return x;
                    }
            ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (Exception ex) {
            String errorMessage = ExceptionUtil.getErrorMessage(ex);
            log.error("AlarmRuleMatchFunction解析上游格式失败，请检查上游是否解析字符串出现问题, 故障原因: {}", errorMessage);
        }
        return pointData;
    }

    /**
     * 组装预匹配的数据
     * @param distributeData
     * @return
     */
    public static DefaultContext<String, Object> assemblePreMatchData(DistributeData distributeData){
        DefaultContext<String, Object> context = new DefaultContext<>();
        try {

            //提取数据字段组装待求值的上下文
            String iotJson = distributeData.getIotJson();
            Map<String, Object> iotColumnValue = CommonConstant.objectMapper.readValue(iotJson, new TypeReference<Map<String, Object>>() {
            });
            iotColumnValue.entrySet().stream().map(x -> {
                        context.put(x.getKey(), String.valueOf(Optional.ofNullable(x.getValue()).orElse("-1L")));
                        return x;
                    }
            ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            String algJson = distributeData.getAlgJson();
            Map<String, Object> algColumnValue = CommonConstant.objectMapper.readValue(algJson, new TypeReference<Map<String, Object>>() {
            });
            algColumnValue.entrySet().stream().map(x -> {
                        context.put(x.getKey(), String.valueOf(Optional.ofNullable(x.getValue()).orElse("-1L")));
                        return x;
                    }
            ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }catch (Exception ex){
            String errorMessage = ExceptionUtil.getErrorMessage(ex);
            log.error("AlarmRuleMatchFunction解析上游格式失败，请检查上游是否解析字符串出现问题, 故障原因: {}", errorMessage);
        }
        return context;
    }


    /**
     * 从状态获取数据组织到内存当中
     * @param currentStateName
     * @param context
     * @param stateDescriptor
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> List<T> fetchCacheState(String currentStateName, FunctionInitializationContext context, ListStateDescriptor<T> stateDescriptor) throws Exception {
        long startTime = System.currentTimeMillis();
        ListState<T> currentState = context.getOperatorStateStore().getListState(stateDescriptor);
        List<T> cacheStateList = new ArrayList<>();
        if (context.isRestored()) {
            for (T element : currentState.get()) {
                cacheStateList.add(element);
            }
            log.info("当前状态: {}, 初始化恢复数据成功:{}|{}ms", currentStateName, cacheStateList.size(), System.currentTimeMillis() - startTime);
        }else {

        }
        return cacheStateList;
    }

    public static <T> ListState<T> fetchCurrentState(FunctionInitializationContext context, ListStateDescriptor<T> stateDescriptor) throws Exception {
        ListState<T> currentState = context.getOperatorStateStore().getListState(stateDescriptor);
        return currentState;
    }

    /**
     * 根据上送的测点或者算法实例id 匹配出对应的ruleId
     * @param pointId 测点id
     * @param algInstanceId 算法实例id
     * @return
     */
    public static AlarmLevelAndTreeSpaceMapping matchAlarmRuleBySourceDataId(Integer pointId, Integer algInstanceId, CacheConfig cacheConfig) throws JsonProcessingException {

        String currentBelongToSpaceId = "";

        if (pointId == null && algInstanceId == null){
            return new AlarmLevelAndTreeSpaceMapping();
        }

        if (pointId != null) {
            List<PointTreeSpace> pointTreeSpaceList = TransferBeanutils.transferDataByList(log, CacheConstant.TERMINAL_POINT_TREE, PointTreeSpaceSingleton.INSTANCE.getInstance(), PointTreeSpace.class, cacheConfig);

            PointOriginData currentPointData = new PointOriginData();
            currentPointData.setPointId(String.valueOf(pointId));
            List<PointTreeSpace> afterFilterDataList = pointTreeSpaceList.stream().filter(x -> {
                List<PointOriginData> pointOriginDataList = x.getPointIds();
                return pointOriginDataList.contains(currentPointData);

            }).collect(Collectors.toList());

            PointTreeSpace pointTreeSpace = afterFilterDataList.get(0);
            currentBelongToSpaceId = pointTreeSpace.getSpaceId();
        }else if (algInstanceId != null){
            //todo 如果当前上送的数据是算法实例id
        }




        //组装成告警等级当前表达式

        List<AlarmDataSourceObject> alarmDataSourceObjectList = TransferBeanutils.transferDataByList(log, CacheConstant.ALARM_RULE_DATA_CACHE_INSTANCE, new AlarmDataSourceObject(), AlarmDataSourceObject.class, cacheConfig);

        if (alarmDataSourceObjectList == null) {
            log.error("当前缓存中还没更新值, 暂时无法匹配到各个告警等级的表达式!, 测点Id: {}, 算法实例id: {}", pointId, algInstanceId);
            return new AlarmLevelAndTreeSpaceMapping();
        }


        //<点位树空间, 告警等级表达式> 的映射关系
        Map<String, AlarmLevelAndTreeSpaceMapping> alarmLevelAndTreeSpaceMappingMap = new HashMap<>();
        AlarmLevelAndTreeSpaceMapping alarmLevelAndTreeSpaceMapping = new AlarmLevelAndTreeSpaceMapping();
        alarmLevelAndTreeSpaceMapping.setSpaceId(currentBelongToSpaceId);
        alarmDataSourceObjectList.forEach(alarmDataSourceObject ->  {
            List<AlarmLevelAndTreeSpaceMapping> alarmLevelAndTreeSpaceMappingList =  alarmDataSourceObject.getAlarmLevelAndTreeSpaceMapping();
            alarmLevelAndTreeSpaceMappingList.forEach(x -> { alarmLevelAndTreeSpaceMappingMap.put(x.getSpaceId(), x); });

        });


        AlarmLevelAndTreeSpaceMapping currentAlarmLevelExpression = alarmLevelAndTreeSpaceMappingMap.get(currentBelongToSpaceId);
        return currentAlarmLevelExpression;
    }
}


