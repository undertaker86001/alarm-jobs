package com.sucheon.alarm.event.rule;

import com.sucheon.alarm.event.alarm.AlarmExpressionObject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 工况表达式
 */
@Getter
@Setter
public class AlarmLevelAndTreeSpaceMapping implements Serializable {

    /**
     * 点位树空间id
     */
    private String spaceId;


    /**
     * 各个报警等级对应的表达式信息
     */
    private List<AlarmExpressionObject> alarmLevelList;

    /**
     * 拓展条件(收敛条件，重置条件，临时告警)
     */
    private AlarmExtCondition extCondition;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlarmLevelAndTreeSpaceMapping that = (AlarmLevelAndTreeSpaceMapping) o;
        return spaceId.equals(that.spaceId);
    }

}
