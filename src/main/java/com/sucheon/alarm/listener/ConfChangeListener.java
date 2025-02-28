package com.sucheon.alarm.listener;

import com.github.jesse.l2cache.Cache;
import com.github.jesse.l2cache.CacheConfig;
import com.sucheon.alarm.config.HttpConf;
import com.sucheon.alarm.constant.CommonConstant;
import com.sucheon.alarm.event.RedisEvent;
import com.sucheon.alarm.event.oc.OcChangeObject;
import com.sucheon.alarm.listener.conf.RedisSubEvent;
import com.sucheon.alarm.utils.ExceptionUtil;
import com.sucheon.alarm.utils.MetadataUtils;
import com.sucheon.alarm.utils.TransferBeanutils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.listener.MessageListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义Redis消息监听器，用观察者模式监听
 */
@Slf4j
public class ConfChangeListener implements MessageListener<String> {


    /**
     * 存放多个配置观察者对象
     */
    private List<ConfObserver> confObserverList = new ArrayList<>();

    /**
     * redis订阅的频道
     */
    private String channel;

    /**
     * 推送的redis发布事件
     */
    private String message;

    /**
     * 保存redis频道和缓存实例的映射关系
     */
    private Map<String, String> channelAndCacheInstanceMapping;


    /**
     * 保存redis频道和http连接之间的关系
     */
    private Map<String, String> channelAndHttpUrlMapping;

    /**
     * 缓存配置
     */
    private CacheConfig cacheConfig;

    /**
     * http连接配置
     */
    private HttpConf httpConf;

    public ConfChangeListener(CacheConfig cacheConfig, HttpConf httpConf){
        this.channelAndCacheInstanceMapping = MetadataUtils.assembleChannelAndCacheInstance();
        this.channelAndHttpUrlMapping = MetadataUtils.assembleChannelAndHttpUrl();
        this.cacheConfig = cacheConfig;
        this.httpConf = httpConf;

    }


    public void attach(ConfObserver observer){
        confObserverList.add(observer);
    }


    public void detach(ConfObserver observer){
        confObserverList.add(observer);
    }


    public RedisSubEvent notifyEvent(){
        RedisSubEvent redisSubEvent = new RedisSubEvent();
        redisSubEvent.setMessage(message);
        redisSubEvent.setChannel(channel);
        return redisSubEvent;
    }

    @Override
    public void onMessage(CharSequence charSequence, String message) {
        String channel = String.valueOf(charSequence);
        this.channel = channel;
        this.message = message;
        channelUpdate(true, channel, message);
        for (ConfObserver observer: confObserverList){
                observer.update(channel, message);
        }
    }

    /**
     * 更新相应的缓存实例的缓存信息
     * @param change
     * @param channel
     * @param message
     */
    public void channelUpdate(boolean change, String channel, String message){
        if (!change){
            return;
        }

        try {
            //更新缓存的消息
            //维护redis频道和发送下游http连接的映射关系
            String cacheInstance = channelAndCacheInstanceMapping.get(channel);
            String httpUrl = channelAndHttpUrlMapping.get(channel);

            RedisEvent redisEvent = CommonConstant.objectMapper.readValue(message, RedisEvent.class);

            HashMap<String, String> header = new HashMap<>();
            HashMap<String, String> params = new HashMap<>();

            //FIXME 修整成符合http参数调用的传参方式
            params.put("instancesIds", CommonConstant.objectMapper.writeValueAsString(redisEvent.getData()));


            //todo 根据redis下发的事件更新消息模版
            Cache cache = TransferBeanutils.accessThirdData(channel, httpUrl, cacheInstance, cacheConfig, httpConf, change, header, params);
            cache.put(cacheInstance, message);
        }catch (Exception ex){
            String errorMessage = ExceptionUtil.getErrorMessage(ex);
            log.error("工况实例更新失败，具体原因是: {}", errorMessage);
        }
    }
}
