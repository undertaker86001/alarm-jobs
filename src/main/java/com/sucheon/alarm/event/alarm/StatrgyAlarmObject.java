package com.sucheon.alarm.event.alarm;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class StatrgyAlarmObject implements Serializable {

    /**
     * 点位树空间id
     */
    private String spaceId;


    /**
     * 命中告警等级后的策略条件
     */
    private String startgyCondition;
}
