package com.sucheon.alarm.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;


@Getter
@Setter
public class RuleMatchResult implements Serializable {

    /**
     * 将报警的计算结果分发给下游哪个kafka
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String topic;

    /**
     * 该算法模版归属的算法组
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String algGroup;

    /**
     * 测点id (上游必传字段)
     */
    @JsonProperty(value = "point_id")
    private String pointId;

    /**
     * 设备上送的通道 (上游必传字段)
     */
    @JsonProperty(value = "device_channel")
    private String deviceChannel;

    /**
     * 设备已经进入kafka的处理时间 (上游必传字段)
     */
    @JsonProperty(value = "device_timestamp")
    private Long deviceTimestamp;


    /**
     * 上送每一笔数据的批次号 (上游必传字段)
     */
    @JsonProperty(value = "batch_id")
    private String batchId;

    /**
     * 算法分组结果，可能会包含多个分组字段
     */
    @JsonProperty(value = "group_key")
    private String groupKey;

    /**
     * iot字段 todo (临时告警链路去掉该字段)
     */
    @JsonProperty(value = "iot_json")
    private String iotJson;

    /**
     * 算法字段 todo (临时告警链路去掉该字段)
     */
    @JsonProperty(value = "alg_json")
    private String algJson;

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
     * 本次推送是否是过线计数 true过线计数/false正常计数
     */
    private Boolean overHeadCount;

    /**
     * 临时告警检测(是否发生异常) 1异常/0非异常
     */
    private Integer exception;

}
