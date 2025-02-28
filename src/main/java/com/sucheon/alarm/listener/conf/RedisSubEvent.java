package com.sucheon.alarm.listener.conf;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 从redis订阅的事件对象
 */
@Getter
@Setter
public class RedisSubEvent implements Serializable {

    /**
     * redis订阅的频道
     */
    private String channel;

    /**
     * redis订阅的消息
     */
    private String message;

}
