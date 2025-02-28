package com.sucheon.alarm.utils;

import com.github.jesse.l2cache.Cache;
import com.github.jesse.l2cache.CacheConfig;
import com.github.jesse.l2cache.builder.CompositeCacheBuilder;
import com.github.jesse.l2cache.cache.expire.DefaultCacheExpiredListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CacheCreateUtils {


    /**
     * 维护需要缓存映射,避免函数调用的时候重复申请cache实例
     */
    private Map<String, Cache> virtualCacheMapping = new HashMap<>();


    /**
     * 根据redis字典构建缓存实例
     * @param cacheName
     * @return
     */
    public Cache compositeCache(String cacheName, CacheConfig cacheConfig){

        if (Objects.isNull(virtualCacheMapping.get(cacheName))) {

            Cache cache = new CompositeCacheBuilder()
                    .setCacheConfig(cacheConfig)
                    .setExpiredListener(new DefaultCacheExpiredListener())
                    .setCacheSyncPolicy(null)
                    .build(cacheName);
            virtualCacheMapping.put(cacheName, cache);
            return cache;
        }else {
            Cache cache = virtualCacheMapping.get(cacheName);
            return cache;
        }
    }
}
