package com.sucheon.alarm.event.alarm;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public class AlarmMatchResult implements Serializable {

    /**
     * 分发到哪个告警的topic
     */
    private String topic;

    /**
     * 生成的雪花id
     */
    private String alarmId;

    /**
     * 设备时间毫秒
     */
    private Long deviceTimestamp;

    /**
     * 告警匹配到规则的命中时间
     */
    private LocalDateTime alarmTime;

    /**
     * 告警等级
     */
    private String alarmLevel;

    /**
     * 告警内容(需要根据告警等级进行匹配)
     */
    private String alarmContext;

    /**
     * 告警规则(来源于数据统一服务当中的报警规则字段)
     */
    private String alarmRule;

    /**
     * 告警拓展信息, json字符串 可以用不同维度统计表
     */
    private String alarmExt;

}
