package com.sucheon.alarm.customenum;

import lombok.Getter;

/**
 * 告警指标优先级枚举类 (0为最高计算优先级，从0,1,2,3,4...逐级降低)
 */
@Getter
public enum MetricPriorityEnum {

    OC_INSTANCE("oc", 0),
    DATA_COLUMN("data_column", 1);


    private MetricPriorityEnum(String metricType, Integer priority){
        this.metricType = metricType;
        this.priority = priority;
    };


    /**
     * 指标类型: 工况/数据字段
     */
    private String metricType;

    /**
     * 指标优先级
     */
    private Integer priority;

}
