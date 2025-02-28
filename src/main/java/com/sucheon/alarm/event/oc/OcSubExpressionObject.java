package com.sucheon.alarm.event.oc;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class OcSubExpressionObject implements Serializable {

    /**
     * 左边子工况表达式
     */
    private String leftSubOcExpression;

    /**
     * 右边子工况表达式
     */
    private String rightSubOcExpression;

    /**
     * 子工况表达式操作符
     */
    private String operator;
}
