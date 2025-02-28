package com.sucheon.alarm.event.alarm;

import lombok.*;

import java.io.Serializable;
import java.util.Map;

/**
 * 每个告警等级所命中的历史最高告警峰值
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmLevelMatchWatermark implements Serializable {

    /**
     * 告警等级
     */
    private String alarmLevel;

    /**
     * <告警key, 历史最高告警峰值>
     */
    private Map<String, Object> alarmLevelRecordHistoryHighValue;

    /**
     * 告警当前命中告警的事件时间
     */
    private Long currentMatchAlarmTime;

    /**
     *  第一次命中该告警等级的事件时间
     */
    private Long firstMatchAlarmTime;


    /**
     * 各个等级是否更新
     */
    private boolean isUpdate;
}
