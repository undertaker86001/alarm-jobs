package com.sucheon.alarm.event.alarm;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 触发报警过线计数的告警上下文
 */
@Getter
@Setter
public class AlarmContext implements Serializable {

    /**
     * 点位id
     */
    private String pointId;

    /**
     * 算法实例id
     */
    private String algInstanceId;

    /**
     * iot字段集合
     */
    private String iotJson;

    /**
     * 算法集合
     */
    private String algJson;

    /**
     * 告警命中时间
     */
    private String alarmTime;

    /**
     * 告警命中的等级
     */
    private String alarmLevel;
}
