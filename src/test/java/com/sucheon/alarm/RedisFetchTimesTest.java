package com.sucheon.alarm;

import com.github.jesse.l2cache.CacheConfig;
import com.github.jesse.l2cache.consts.CacheType;
import com.sucheon.alarm.constant.RedisConstant;
import org.junit.Before;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

/**
 * 写redis出现异常次数
 */
public class RedisFetchTimesTest {


    private static final String TEST_KEY = "redisson";

    private static final long TEST_VALUE = 100L;

    //redis客户端
    private RedissonClient redissonClient;


    @Before
    public void setUp(){
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setCacheType(CacheType.REDIS.name())
                .getRedis()
                .setExpireTime(30000000)
                .setLock(true)
                .setRedissonYamlConfig("redisson.yaml");
        redissonClient = Redisson.create(cacheConfig.getRedis().getRedissonConfig());
    }

    @Test
    public void testRedisWriteFetchTimes(){

        RTopic topic = redissonClient.getTopic(RedisConstant.ALARM_RULE);
        topic.publish("{\n" +
                "\"code\":\"B0120022\",\n" +
                "\"data\":[\n" +
                "\n" +
                " {\n" +
                " \"pointId\":12311,\n" +
                " \"deviceChannel\":\"sc01234243-01\",\n" +
                " \"notStoreTemplateId\":85662222177\n" +
                " },\n" +
                "{\n" +
                " \"pointId\":12311,\n" +
                " \"deviceChanle\":\"sc01234243-01\",\n" +
                " \"notStoreTemplateId\":85662222177\n" +
                "}]}");


    }
}
