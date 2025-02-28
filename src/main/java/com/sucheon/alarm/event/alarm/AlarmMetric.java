package com.sucheon.alarm.event.alarm;

import com.sucheon.alarm.event.OcDataColumnMetric;
import com.sucheon.alarm.event.oc.OcSubExpressionObject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 告警metric的通用bean(工况表达式和数据字段阈值计算表达式)
 * 记录每个告警等级下的 <告警指标,告警表达式,优先级>
 */
@Getter
@Setter
public class AlarmMetric implements Serializable {

    /**
     * 告警指标
     */
    private String metricName;

    /**
     * 告警表达式 （新建工况实例时，定义的表达式 avgspeed < 10 && load < 1, 该字段仅限工况定义时使用）
     */
    private String metricExpression;

    /**
     * 告警指标优先级
     */
    private Integer metricPriority;

    /**
     * 告警的工况子表达式组合计算对象（比如 oc_key_field("xdsdsd-fsfsf_fsfd", "rms", "highspeed") > 100
     * && oc_key_field("xdsdsd-fsfsf_fsfd", "rms", "lowspeed ）> 29 )
     */
    private List<OcDataColumnMetric> ocDataColumnMetricList;
}
