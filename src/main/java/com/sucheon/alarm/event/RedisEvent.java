package com.sucheon.alarm.event;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class RedisEvent implements Serializable {

    /**
     * 配置项
     */
    private String config;


    /**
     * 事件
     */
    private String event;


    /**
     * 更新的工况实例列表
     */
    private List<String> data;
}
