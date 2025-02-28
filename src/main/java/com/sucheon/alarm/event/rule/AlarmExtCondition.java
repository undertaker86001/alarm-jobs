package com.sucheon.alarm.event.rule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AlarmExtCondition implements Serializable {

    private String convergenceConditions;

    private String resetConditions;

    private String temporaryAlarm;

}
