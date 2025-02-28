package com.sucheon.alarm.config;

import cn.hutool.core.util.StrUtil;
import com.github.jesse.l2cache.CacheConfig;
import com.github.jesse.l2cache.consts.CacheType;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * 联动缓存配置工具类
 */
@Configuration
@Data
public class CacheUtils {

    /**
     * 一级缓存配置
     */
    @Value("${ddps.caffeine.l1-cache-name}")
    private String l1CacheName;

    /**
     * 二级缓存配置
     */
    @Value("${ddps.caffeine.l2-cache-name}")
    private String l2CacheName;

    /**
     * 是否全部启用一级缓存，默认false
     */
    @Value("${ddps.caffeine.l1-all-open}")
    private String l1AllOpen;

    /**
     * 是否自动刷新过期缓存
     */
    @Value("${ddps.caffeine.auto-refresh-expire-cache}")
    private boolean autoRefreshExpireCache = true;


    //################### redis部分配置#############################3

    /**
     * 过期时间
     */
    @Value("${ddps.redis.expire-time}")
    private String expireTime;

    /**
     * 加载数据的时候，是否需要加锁
     */
    @Value("${ddps.redis.lock}")
    private String lock;


    CacheConfig cacheConfig = new CacheConfig();


    /**
     * 构建组合缓存配置
     * @return
     */
    public CacheConfig getCacheConfig(){
        cacheConfig.setCacheType(CacheType.COMPOSITE.name())
                .getComposite()
                .setL1CacheType(this.getL1CacheType())
                .setL2CacheType(this.getL2CacheType())
                .setL1AllOpen(Boolean.parseBoolean(Optional.of(l1AllOpen).orElse("false")));

        //本地缓存信息注册
        cacheConfig.getCaffeine()
                .setDefaultSpec("initialCapacity=10,maximumSize=200,refreshAfterWrite=2s,recordStats")
                .setAutoRefreshExpireCache(autoRefreshExpireCache);
        //远程缓存信息注册
        cacheConfig.setCacheType(CacheType.REDIS.name())
                .setAllowNullValues(true)
                .getRedis()
                .setExpireTime(Long.parseLong(this.getExpireTime()))
                .setLock(Boolean.parseBoolean(Optional.of(this.getLock()).orElse("false")))
                .setRedissonYamlConfig("redisson.yaml");


        return cacheConfig;
    }

    public String getL1CacheType(){
        return StrUtil.isBlank(l1CacheName) ? CacheType.CAFFEINE.name() : CacheType.getCacheType(l1CacheName.toLowerCase()).name();
    }

    public String getL2CacheType(){
        return StrUtil.isBlank(l2CacheName) ? CacheType.REDIS.name() : CacheType.getCacheType(l2CacheName.toLowerCase()).name();
    }


}
