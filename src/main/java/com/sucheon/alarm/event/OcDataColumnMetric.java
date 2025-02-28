package com.sucheon.alarm.event;

import com.sucheon.alarm.event.alarm.AlarmOcExpression;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 工况数据字段metric
 */
@Getter
@Setter
public class OcDataColumnMetric implements Serializable {

    /**
     * 解析后的左工况子表达式
     */
    private AlarmOcExpression leftOcExpression;

    /**
     * 解析后的右工况子表达式
     */
    private AlarmOcExpression rightOcExpression;

    /**
     * 子工况表达式操作符
     */
    private String operator;

}
