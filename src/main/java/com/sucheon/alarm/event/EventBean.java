package com.sucheon.alarm.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 需要用JsonfieldConverter转换下划线
 */
public class EventBean implements Serializable {


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
     * 区分上送数据的来源 (在上送数据的时候不需要序列化)
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String origin;

    /**
     * 分发的点位编码 在算法的配置字典中维护 (在上送数据的时候不需要序列化)
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String code;


    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPointId() {
        return pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }

    public String getDeviceChannel() {
        return deviceChannel;
    }

    public void setDeviceChannel(String deviceChannel) {
        this.deviceChannel = deviceChannel;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public Long getDeviceTimeStamp() {
        return deviceTimestamp;
    }

    public void setDeviceTimeStamp(Long deviceTimestamp) {
        this.deviceTimestamp = deviceTimestamp;
    }
}
