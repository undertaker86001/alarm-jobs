package com.sucheon.alarm.singleton;

import com.sucheon.alarm.event.oc.OcAlarmRuleMapping;
import com.sucheon.alarm.event.rule.AlarmRuleMapping;

public enum AlarmRuleMappingSingleton {

    INSTANCE;
    private AlarmRuleMapping alarmRuleMappingInstance;
    AlarmRuleMappingSingleton(){
        alarmRuleMappingInstance = new AlarmRuleMapping();
    }

    public AlarmRuleMapping getInstance(){
        return alarmRuleMappingInstance;
    }
}
