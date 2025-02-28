package com.sucheon.alarm.event.oc;

import com.sucheon.alarm.event.alarm.AlarmExpressionObject;
import com.sucheon.alarm.event.rule.AlarmExtCondition;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class OcChangeObject implements Serializable {


    /**
     * 点位树空间id
     */
    private String spaceId;

    /**
     * 工况实例列表
     */
    private List<OcInstance> ocInstanceList;


    /**
     * 告警等级表达式
     */
    private List<AlarmExpressionObject> alarmLevelList;

    /**
     * 拓展条件(收敛条件，重置条件，临时告警)
     */
    private AlarmExtCondition extCondition;
}



