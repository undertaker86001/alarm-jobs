package com.sucheon.alarm.event.alarm;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 用户在区分工况的情况下，需要编写的最上层的表达式对象
 */
@Getter
@Setter
public class AlarmOcExpression implements Serializable {

    /**
     * 绑定的数据树节点编号
     */
    private String code;

    /**
     * 选择的数据字段
     */
    private String dataColumn;

    /**
     * 工况表达式的rule 比如highspeed
     */
    private String ocRuleName;

    /**
     * 数据字段比较操作符
     */
    private String dataColumnOperator;

    /**
     * 数据字段的值
     */
    private String dataColumnValue;
}
