package com.sucheon.alarm.event;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExpressionContext {

    /**
     * 读取的子表达式
     */
    private String expression;

    /**
     * 节点类型 操作符/常量/变量
     */
    private String type;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 从内到外存放操作符
     */
    private List<String> belongs;

}
