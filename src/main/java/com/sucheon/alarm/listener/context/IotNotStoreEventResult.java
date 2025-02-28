package com.sucheon.alarm.listener.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sucheon.alarm.listener.context.result.PointTreeSpaceInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 边缘端上送的点位数据不入库的结果
 */
@Getter
@Setter
public class IotNotStoreEventResult implements Serializable {


    /**
     * http请求的自定义编号，无意义
     */
    private String code;

    /**
     * 下游数据需要区分是什么类型的数据做对应替换
     */
    @JsonIgnore
    private String channel;

    /**
     * 变更字段
     */
    private List<PointTreeSpaceInfo> data;
}
