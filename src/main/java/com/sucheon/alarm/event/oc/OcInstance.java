package com.sucheon.alarm.event.oc;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 工况实例
 */
@Getter
@Setter
public class OcInstance {

    /**
     * 工况实例id
     */
    private Integer instanceId;

    /**
     * 变更的工况表达式
     */
    private List<OcExpression> ocExpressionList;
}
