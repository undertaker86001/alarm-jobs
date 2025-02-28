package com.sucheon.alarm.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class DistributeData implements Serializable {

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
     * iot字段
     */
    @JsonProperty(value = "iot_json")
    private String iotJson;

    /**
     * 算法字段
     */
    @JsonProperty(value = "alg_json")
    private String algJson;
}
