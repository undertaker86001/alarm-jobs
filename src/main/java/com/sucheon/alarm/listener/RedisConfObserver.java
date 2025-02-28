package com.sucheon.alarm.listener;


import com.sucheon.alarm.listener.conf.RedisSubEvent;

/**
 * redis配置变更监听器
 */
public class RedisConfObserver implements ConfObserver {



    @Override
    public RedisSubEvent update(String channel, String message) {
        RedisSubEvent redisSubEvent = new RedisSubEvent();
        redisSubEvent.setChannel(channel);
        redisSubEvent.setMessage(message);
        return redisSubEvent;
    }
}
