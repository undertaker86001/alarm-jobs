package com.sucheon.alarm.event.alarm;

import com.ql.util.express.ExpressRunner;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TemporaryAlarm implements Serializable {


    /**
     * 临时报警要推送到哪一个topic
     */
    private String topic;

    /**
     * 过线计算触发告警等级的时间
     */
    private String alarmTime;

    /**
     * 当前命中告警等级
     */
    private String alarmLevel;


    /**
     * 过线计数的告警Id
     */
    private String alarmId;

    /**
     * 数据是否过线，过线为true
     */
    private Boolean overHeadCount;

    /**
     * 临时告警检测数据是否异常
     */
    private Integer exception;

}
