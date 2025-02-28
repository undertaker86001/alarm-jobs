package com.sucheon.alarm.event.oc;

import lombok.Getter;
import lombok.Setter;

/**
 * 工况表达式
 */
@Getter
@Setter
public class OcExpression {

    /**
     * 工况指标 (比如highspeed)
     */
    private String ocKey;

    /**
     * 工况表达式(avgspeed>5&&load>1)
     */
    private String expr;

    /**
     * 工况实例id
     */
    private Integer ocInstanceId;

}
