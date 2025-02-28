package com.sucheon.alarm.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.sucheon.alarm.constant.CommonConstant;
import com.sucheon.alarm.constant.FieldConstants;
import com.sucheon.alarm.event.RuleMatchResult;
import com.sucheon.alarm.event.alarm.AlarmLevelMatchWatermark;
import com.sucheon.alarm.event.alarm.AlarmMatchCacheInterval;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 根据不同告警等级合并新到达的数据和旧数据
 */
@Slf4j
public class MergeAlarmDataUtils {


    /**
     * 新旧数据比对,得到当前告警等级的各个指标值
     * @param oldData
     * @param newData
     * @return
     */
    public static AlarmLevelMatchWatermark mergeData(AlarmLevelMatchWatermark oldData, RuleMatchResult newData) throws IllegalAccessException {
        AlarmLevelMatchWatermark alarmLevelMatchWatermark = new AlarmLevelMatchWatermark();
        if ( oldData == null || InternalTypeUtils.iaAllFieldsNull(oldData)){
            Map<String, Object> oldDataMap = ReflectUtils.convertMap(newData);
            alarmLevelMatchWatermark.setAlarmLevelRecordHistoryHighValue(oldDataMap);
            alarmLevelMatchWatermark.setUpdate(false);
        }else {

            Map<String, Object> currentOldData = oldData.getAlarmLevelRecordHistoryHighValue();
            //从新数据当中找到原始数据
            Map<String, Object> currentNewData = AssembleDataUtil.assembleOverheadData(newData);


            for (Map.Entry<String, Object> entry : currentNewData.entrySet()){
                String key = entry.getKey();
                Object value1 = entry.getValue();
                Object value2 = currentOldData.get(key);

                if (value2 != null && InternalTypeUtils.iaAllFieldsNull(value2)){
                    Comparator<Object> comparator = CompareUtils.getComparator(value1.getClass(), value2.getClass());
                    if (comparator.compare(value2, value1) < 0){
                        entry.setValue(value1);
                    }
                }
            }

            alarmLevelMatchWatermark.setAlarmLevelRecordHistoryHighValue(currentNewData);
            alarmLevelMatchWatermark.setUpdate(true);
        }

        return alarmLevelMatchWatermark;
    }

    /**
     * 根据更新过线的告警等级和告警时间
     * @param oldData
     * @param newData
     * @return
     */
    public static AlarmLevelMatchWatermark updateAlarmTimeByLevel(AlarmLevelMatchWatermark oldData, RuleMatchResult newData) throws JsonProcessingException {

        Map<String, Object> currentData =  oldData.getAlarmLevelRecordHistoryHighValue();

        Object deviceTimestampObject = currentData.get(FieldConstants.DEVICE_TIMESTAMP);

        Long countOverheadLineDeviceTimestamp = 0L;
        if (deviceTimestampObject != null) {
            countOverheadLineDeviceTimestamp = Long.valueOf(String.valueOf(deviceTimestampObject));
        }else {

            log.error("当前设备上送的时间戳为空 或者 连续一周没有从橙盒上送数据, 请检查原始数据是否有问题!, 原始数据：{}", CommonConstant.objectMapper.writeValueAsString(newData));
        }

        Long deviceTimestamp = newData.getDeviceTimestamp();

        if (oldData.getFirstMatchAlarmTime() == null){
            oldData.setFirstMatchAlarmTime(deviceTimestamp);
            oldData.setCurrentMatchAlarmTime(deviceTimestamp);
        }else {
            oldData.setCurrentMatchAlarmTime(deviceTimestamp);
        }
        return oldData;
    }


    /**
     * 对所有告警等级的时间做区间合并，找到初次告警时间和最后告警时间
     * @param alarmTimeByLevel
     * @return
     */
    public static AlarmMatchCacheInterval alarmMatchCacheInterval(List<AlarmLevelMatchWatermark> alarmTimeByLevel){

        Map<Long, Long> firstAndListTimestampByLevel = new HashMap<>();
        for (AlarmLevelMatchWatermark alarmLevelMatchWatermark: alarmTimeByLevel){
            firstAndListTimestampByLevel.put(alarmLevelMatchWatermark.getFirstMatchAlarmTime(), alarmLevelMatchWatermark.getCurrentMatchAlarmTime());
        }

        if (firstAndListTimestampByLevel.size() == 0){
            return new AlarmMatchCacheInterval(0L, 0L);
        }

        //按照升序排序

        Collections.sort(alarmTimeByLevel, CompareUtils::compareAlarmTime);

        AlarmLevelMatchWatermark last = alarmTimeByLevel.get(0);
        for (int i =1; i< alarmTimeByLevel.size(); i++){
            AlarmLevelMatchWatermark curt = alarmTimeByLevel.get(i);
            if (curt.getFirstMatchAlarmTime() <= last.getCurrentMatchAlarmTime()){
                last.setCurrentMatchAlarmTime( Math.max(last.getCurrentMatchAlarmTime(), curt.getCurrentMatchAlarmTime()) );
            } else {
                alarmTimeByLevel.add(last);
                last = curt;
            }
        }
        alarmTimeByLevel.add(last);

        Long lastAlarmTime = last.getCurrentMatchAlarmTime();
        Long firstAlarmTime = alarmTimeByLevel.get(0).getFirstMatchAlarmTime();
        AlarmMatchCacheInterval matchCacheInterval = new AlarmMatchCacheInterval();
        matchCacheInterval.setFirstAlarmTime(firstAlarmTime);
        matchCacheInterval.setLastAlarmTime(lastAlarmTime);
        return matchCacheInterval;

    }


}
