package com.sucheon.alarm.event.rule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public class AlarmRuleMapping implements Serializable {

    /**
     * 报警规则id
     */
    private String alarmRuleId;

    /**
     * 工况实例id
     */
    private String ocInstanceId;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlarmRuleMapping that = (AlarmRuleMapping) o;
        return Objects.equals(alarmRuleId, that.alarmRuleId) && Objects.equals(ocInstanceId, that.ocInstanceId);
    }
}
