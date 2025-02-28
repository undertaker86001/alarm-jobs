package com.sucheon.alarm.event;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 要传递给下游算法侧框架的数据
 */
@Getter
@Setter
public class AlgData implements Serializable {

    /**
     * 当前算法实例所归属的节点编号
     */
    private String pointId;

    /**
     * 设备通道唯一标识
     */
    private String deviceChannel;

    /**
     * 算法组设置的分组字段
     */
    private String groupKey;

    /**
     * 当前算法实例绑定的节点编号
     */
    private String code;

    /**
     * 设备上报时间戳
     */
    private Long deviceTimestamp;

    /**
     * 批次号
     */
    private String batchId;


}
