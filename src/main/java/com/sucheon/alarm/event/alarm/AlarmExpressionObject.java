package com.sucheon.alarm.event.alarm;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * 告警表达式对象
 */
@Getter
@Setter
public class AlarmExpressionObject {

    /**
     * 告警等级
     */
    private String alarmLevel;

    /**
     * 告警模版对应的文案
     */
    private String alarmKey;

    /**
     * 告警的多数据字段阈值线表达式
     */
    private String alarmThersoldExpression;
}
