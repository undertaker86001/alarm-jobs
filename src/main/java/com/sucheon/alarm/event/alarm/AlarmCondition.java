package com.sucheon.alarm.event.alarm;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 告警条件
 */
@Getter
@Setter
public class AlarmCondition implements Serializable {

    /**
     * 收敛条件
     */
    private String convergenceCondition;

    /**
     * 重置策略
     */
    private String resetCondition;

    /**
     * 临时报警命中条件
     */
    private String temporaryAlarm;

    /**
     * 是否开启临时告警推送
     */
    private String notifyOpen;

}
