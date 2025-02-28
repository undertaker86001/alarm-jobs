package com.sucheon.alarm.event.alarm;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class StartgyCondition implements Serializable {

    /**
     * 收敛条件
     */
    private String convergenceCondition;

    /**
     * 重置条件
     */
    private String resetCondition;


    /**
     * 临时告警命中表达式
     */
    private String temporaryAlarm;

    /**
     * 是否开启临时告警
     */
    private String notifyOpen;
}
