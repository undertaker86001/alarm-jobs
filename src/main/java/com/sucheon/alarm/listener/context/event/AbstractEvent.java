package com.sucheon.alarm.listener.context.event;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AbstractEvent<T> {

    /**
     * 配置项
     */
    private String config;

    /**
     * 操作
     */
    private String event;

    private List<T> data;

}
