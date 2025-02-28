package com.sucheon.alarm.utils;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.jesse.l2cache.Cache;
import com.github.jesse.l2cache.CacheConfig;

import com.sucheon.alarm.config.HttpConf;
import com.sucheon.alarm.config.ObjectSingleton;
import com.sucheon.alarm.constant.CacheConstant;
import com.sucheon.alarm.constant.CommonConstant;
import com.sucheon.alarm.event.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.sucheon.alarm.constant.FieldConstants.*;
import static com.sucheon.alarm.utils.InternalTypeUtils.iaAllFieldsNull;


/**
 * 转换规则匹配的bean
 */
public class TransferBeanutils {

    /**
     * 根据不同的redis订阅频道组装配置下发对象
     * @param cacheConfig 传入的cacheConfig配置
     * @param subEvent
     * @param originData
     * @param resultData
     * @param <T>
     * @param <R>
     * @return
     * @throws IOException
     */
    public static <T, R> String assemblePreSendContext(String httpUrl, String cacheInstance, CacheConfig cacheConfig, HttpConf httpConf, boolean subEvent, T originData, R resultData) throws IOException {

        if(!subEvent){
            return "";
        }

        //构建本地缓存+ 远程缓存
        Cache cache = accessThirdData(cacheInstance, httpUrl, cacheInstance,  cacheConfig, httpConf, true, new HashMap<>(), new HashMap<>());
        //如果http接口请求失败，走旧的缓存
        Object confResult = cache.get(cacheInstance);

        Object eventResult = CommonConstant.objectMapper.readValue(String.valueOf(confResult), resultData.getClass());

        //需要设置从哪个频道更新的在下游更新对应的数据
        InternalTypeUtils.addField(eventResult, REDIS_CACHE_INSTANCE, cacheInstance);

        String sendFinalResult = CommonConstant.objectMapper.writeValueAsString(eventResult);
        return sendFinalResult;
    }


    /**
     * 更新缓存中待推送的事件，判断是否应该调用
     *
     * @param change Redis是否变更订阅
     * @param channel 需要往下游告知的频道名
     * @param cacheInstance 当前缓存实例
     * @param cacheConfig 配置的缓存配置
     * @param httpConf 配置的http链接参数
     * @param change 是否配置发生变化
     * @param params http请求的餐素
     * @param header
     * @return 根据情况判断哪些字段不需要分发
     */
    public static Cache accessThirdData(String channel, String httpUrl, String cacheInstance, CacheConfig cacheConfig, HttpConf httpConf, boolean change,
                                        HashMap<String, String> params, HashMap<String, String> header){
        //构建本地缓存+ 远程缓存
        Cache cache = accessCurrentCacheInstance(cacheInstance, cacheConfig);
        String currentResult = "";
        if (change) {
            //请求http接口 支持重试
            String[] result = RetryUtils.executeWithRetry(
                    () -> HttpAsyncClientUtil.getAsyncHttpResult(httpUrl, params, header, 1),
                    Integer.parseInt(httpConf.getRetryTimes()),
                    Long.parseLong(httpConf.getSleepTimeInMilliSecond()),
                    Boolean.parseBoolean(httpConf.getExponential()));
            currentResult = result[0];
            cache.put(cacheInstance, currentResult);
        }

        return cache;
    }

    /**
     * 访问第三方缓存实例
     * @param cacheInstance 缓存实例
     * @param cacheConfig 缓存配置
     * @return
     */
    public static Cache accessCurrentCacheInstance(String cacheInstance, CacheConfig cacheConfig){
        Cache cache = CacheUtils.compositeCache(cacheInstance, cacheConfig);
        return cache;
    }


    /**
     * 为Object对象创建基于枚举类型的单例模式
     * @return
     */
    public static Object getObjectInstance(){
        return ObjectSingleton.INSTANCE.getInstance();
    }

    /**
     * 从缓存提取对应访问http接口的响应
     * 并转换对应的数据类型
     * @param logger
     * @param cacheInstance
     * @param object
     * @param tClass
     * @param cacheConfig
     * @param <T>
     * @return
     * @throws JsonProcessingException
     */
    public static <T> List<T> transferDataByList(Logger logger, String cacheInstance, T object, Class<T> tClass, CacheConfig cacheConfig) throws JsonProcessingException {
        Cache cache = TransferBeanutils.accessCurrentCacheInstance(cacheInstance, cacheConfig);
        Object pointTree = cache.get(cacheInstance);
        List<Object> pointTreeSpace = CommonConstant.objectMapper.readValue(String.valueOf(pointTree), List.class);
        List<T> afterList = pointTreeSpace.stream().map(x -> {
            try {
                return CommonConstant.objectMapper.readValue(String.valueOf(x), tClass);
            } catch (JsonProcessingException e) {
                logger.error("当前解析点位树空间响应实体");
            }
            return object;
        }).collect(Collectors.toList());
        return afterList;
    }


}
