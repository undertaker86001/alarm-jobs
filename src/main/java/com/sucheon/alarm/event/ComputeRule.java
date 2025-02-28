package com.sucheon.alarm.event;

import com.sucheon.alarm.event.alarm.SubExpression;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


/**
 * 告警规则计算公式
 */
@Getter
@Setter
public class ComputeRule implements Serializable {


    /**
     * 是否存在优先级
     */
    private Integer proprioty;

    /**
     * 子表达式
     */
    private SubExpression computeRule;

}
