package com.sucheon.alarm.utils;


import com.sucheon.alarm.constant.RedisConstant;
import com.sucheon.alarm.listener.conf.RedisSubEvent;
import com.sucheon.alarm.listener.ConfChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RedisSubPubUtils {


    /**
     * 根据不同的redis频道绑定对应的观察者，找到对应的监听器
     * @param redissonClient
     * @param confChangeListener
     * @return
     */
    public static RedisSubEvent attachTopicAndResultSubEvent(String channelName, RedissonClient redissonClient, ConfChangeListener confChangeListener){
        //消息发布主题
        RTopic topic = redissonClient.getTopic(channelName);

        //关联redis订阅事件，先注册监听器得到异步计算出结果后，再开始下一个异步计算
        CompletableFuture<Void> cfStage1 = CompletableFuture.completedFuture(topic)
                .thenAcceptAsync( rTopic ->{
                    rTopic.addListener(String.class, confChangeListener);
                } );

        CompletableFuture<RedisSubEvent> cfStage2 = cfStage1.thenApply(rtopic -> confChangeListener.notifyEvent());

        //获取最终结果，阻塞当前线程
        RedisSubEvent subEvent = cfStage2.join();
        if (subEvent ==null || InternalTypeUtils.iaAllFieldsNull(subEvent)) {
            return new RedisSubEvent();
        }

        //对应不同的链路 边缘端或者算法端
        if (StringUtils.isBlank(subEvent.getMessage())) {
            return new RedisSubEvent();
        }
        return subEvent;
    }


    /**
     * 监听所有频道，得到更新的事件
     * @param redissonClient
     * @param confChangeListener
     * @return
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static Map<String, RedisSubEvent> pollRedisEvent(RedissonClient redissonClient, ConfChangeListener confChangeListener) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        Map<String, RedisSubEvent> updateEventCollection = new HashMap<>();

        List<String> channelList = assembleChannelList();
        for (String channelName : channelList){
            RedisSubEvent redisSubEvent = attachTopicAndResultSubEvent(channelName, redissonClient, confChangeListener);
            updateEventCollection.put(channelName, redisSubEvent);
        }
        return updateEventCollection;
    }

    public static List<String> assembleChannelList() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        RedisConstant redisConstant = new RedisConstant();
        List<String> channelList = redisConstant.allChannelNames();
        return channelList;
    }
}
