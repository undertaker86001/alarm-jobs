package com.sucheon.alarm.event.alarm;

import lombok.Getter;
import lombok.Setter;

/**
 * 子表达式
 */
@Getter
@Setter
public class SubExpression {

    /**
     * 比较操作符
     */
    private String operator;

    /**
     * 变量
     */
    private String variable;

    /**
     * 常量
     */
    private String constant;
}
