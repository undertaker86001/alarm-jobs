package com.sucheon.alarm.event.oc;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 工况和告警规则的映射（全量）
 */
@Getter
@Setter
public class OcAlarmRuleMapping implements Serializable {



    /**
     * 工况实例id
     */
    private String ocInstanceId;

    /**
     *
     */
    private String algInstanceId;

}
