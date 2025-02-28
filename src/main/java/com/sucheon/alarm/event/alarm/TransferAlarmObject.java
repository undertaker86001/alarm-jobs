package com.sucheon.alarm.event.alarm;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * 定义工况与字段阈值线的计算优先级
 */
@Getter
@Setter
public class TransferAlarmObject implements Serializable {

    /**
     * 工况表达式以及计算权重
     */
    public Map<String,Integer> ocExpressionProprity;
}
