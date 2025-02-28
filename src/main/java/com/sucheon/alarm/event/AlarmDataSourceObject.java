package com.sucheon.alarm.event;


import com.sucheon.alarm.event.rule.AlarmLevelAndTreeSpaceMapping;
import com.sucheon.alarm.event.rule.AlarmRuleMapping;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 告警规则与数据源关系变更对象
 */
@Getter
@Setter
public class AlarmDataSourceObject implements Serializable {

    private List<AlarmLevelAndTreeSpaceMapping> alarmLevelAndTreeSpaceMapping;


    private List<AlarmRuleMapping> alarmRuleMapping;
}
