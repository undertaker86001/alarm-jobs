package com.sucheon.alarm.listener;


import com.sucheon.alarm.listener.conf.RedisSubEvent;

/**
 * 配置观察者，监听配置变更的消息
 */
public interface ConfObserver {

    /**
     * 监听从上游配置变更的到的消息(目前只针对redis类型)
     * @param channel 订阅的redis频道
     * @param  message 订阅的redis消息
     */
    RedisSubEvent update(String channel, String message);
}
