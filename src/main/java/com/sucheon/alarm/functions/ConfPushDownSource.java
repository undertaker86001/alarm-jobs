package com.sucheon.alarm.functions;

import com.github.jesse.l2cache.CacheConfig;
import com.sucheon.alarm.config.HttpConf;
import com.sucheon.alarm.constant.RedisConstant;
import com.sucheon.alarm.event.NetworkConf;
import com.sucheon.alarm.listener.ConfChangeListener;
import com.sucheon.alarm.listener.RedisConfObserver;
import com.sucheon.alarm.listener.conf.RedisSubEvent;
import com.sucheon.alarm.listener.context.IotNotStoreEventResult;
import com.sucheon.alarm.listener.context.event.PointNotStoreEvent;
import com.sucheon.alarm.utils.ExceptionUtil;
import com.sucheon.alarm.utils.MetadataUtils;
import com.sucheon.alarm.utils.RedisSubPubUtils;
import com.sucheon.alarm.utils.TransferBeanutils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 配置下推数据源
 */
@Slf4j
public class ConfPushDownSource extends RichSourceFunction<String> {

    /**
     * redisson客户端
     */
    private RedissonClient redissonClient;

    /**
     * 配置所有的缓存配置
     */
    private CacheConfig cacheConfig;

    /**
     * 获取订阅redis的所有频道和调用的http连接
     */
    private Map<String, String> channelAndHttpUrl;


    /**
     * 获取订阅redis的所有频道和对应的缓存实例，避免手动替换
     */
    private Map<String, String> channelAndCacheInstance;

    /**
     * 每个redis频道是否是第一次更新
     */
    private Map<String, AtomicInteger> channelEffectCount;

    /**
     * 回调第三方接口的网络配置
     */
    private String thirdAddress;

    /**
     * 事件变更监视器
     */
    private ConfChangeListener confChangeListener;


    /**
     * 设置内外网转换配置
     */
    private HttpConf httpConf;

    @SneakyThrows
    public ConfPushDownSource(CacheConfig cacheConfig, NetworkConf networkConf){
        this.cacheConfig = cacheConfig;
        this.channelEffectCount = new HashMap<>();

        List<String> channelMapping = RedisSubPubUtils.assembleChannelList();
        for (String channel: channelMapping) {
            this.channelEffectCount.computeIfAbsent(channel, (k) -> new AtomicInteger(0));
        }
        this.channelAndHttpUrl = MetadataUtils.assembleChannelAndHttpUrl();
        this.channelAndCacheInstance = MetadataUtils.assembleChannelAndCacheInstance();
        this.thirdAddress = MetadataUtils.assembleHttpUrl(networkConf.getAddress(), networkConf.getPort());

        this.httpConf = HttpConf.builder().build();
    }

    @Override
    public void open(Configuration parameters){

        CacheConfig cacheConfig = this.cacheConfig;
        redissonClient = Redisson.create(cacheConfig.getRedis().getRedissonConfig());
        ConfChangeListener confChangeListener = new ConfChangeListener(cacheConfig, httpConf);
        confChangeListener.attach(new RedisConfObserver());
        this.confChangeListener = confChangeListener;

    }

    //下游只需要知道是哪个channel变更的消息 取缓存里取就可以
    @Override
    public void run(SourceContext<String> context)  {
        try {
            String sendFinalResult = "";

            // 当有新配置写入的时候，替换缓存里的配置
            Map<String, RedisSubEvent> subEventMap = RedisSubPubUtils.pollRedisEvent(redissonClient, confChangeListener);
            for (Map.Entry<String, RedisSubEvent> event : subEventMap.entrySet()) {
                String channel = event.getKey();
                RedisSubEvent subEvent = event.getValue();

                RedisSubEvent changeSymbol = event.getValue();
                String currentHttpUri = channelAndHttpUrl.get(channel) == null ? "" : channelAndHttpUrl.get(channel);
                String currentCacheInstance = channelAndCacheInstance.get(channel) == null ? "" : channelAndCacheInstance.get(channel);
                String currentHttpAddress = thirdAddress + currentHttpUri;


                //监听所有频道 判断该频道是否是第一次订阅
                AtomicInteger channelCount = channelEffectCount.get(channel);
               if (channel.equals(RedisConstant.ALG_OC_RULE)) {
                    //todo 算法实例和数据源关系变更
                    sendFinalResult = TransferBeanutils.assemblePreSendContext(currentHttpAddress, currentCacheInstance, cacheConfig, null, false, null, null);
                }else if (channel.equals("oc")){
                }//todo 算法实例和工况绑定关系变更

                context.collect(sendFinalResult);
            }
        }catch (Exception ex){
            String errorMessage = ExceptionUtil.getErrorMessage(ex);
            log.error("告警服务订阅相关配置失败， 原因为{}", errorMessage);
        }


    }

    @Override
    public void cancel() {
        //异步延时关闭
        redissonClient.shutdown();
    }
}
