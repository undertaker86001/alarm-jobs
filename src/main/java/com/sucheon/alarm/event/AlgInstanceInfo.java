package com.sucheon.alarm.event;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 根据每个算法实例组装对应的字段，code编号信息
 */
@Getter
@Setter
public class AlgInstanceInfo implements Serializable {

    /**
     * 归属的节点树编号
     */
    private String code;

    /**
     * 当前的算法实例
     */
    private String instanceId;

    /**
     * 上游的算法组实例信息
     */
    private List<String> prevInstanceList;


    /**
     * 每个字段所归属的上游算法实例信息
     */
    private Map<String, String> fieldBelongToInstance;

    /**
     * 每个字段对应的算法组名称
     */
    private Map<String, String> fieldGroup;

}
