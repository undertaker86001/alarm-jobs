package com.sucheon.alarm.constant;

/**
 * 公共字段名列表
 */
public class FieldConstants {

    //################################ 上送公共兼容字段 #################################################
    public static final String DEVICE_TIMESTAMP = "device_timestamp";

    public static final String POINT_ID = "point_id";

    public static final String BATCH_ID = "batch_id";

    public static final String DEVICE_CHANNEL = "device_channel";

    public static final String ALG_INSTANCE_ID = "alg_instance_id";


    //############################## 分发系统内部字段 #################################


    public static final String TOPIC = "topic";

    /**
     * 从算法结果当中获取的数据
     */
    public static final String RESULT_DATA = "resultData";

    /**
     * 节点树编号
     */
    public static final String NODE_CODE = "code";

    /**
     * 上送的来源数据
     */
    public static final String ORIGIN_DATA = "origin";

    /**
     * 订阅redis的频道
     */
    public static final String REDIS_CACHE_INSTANCE = "cacheInstance";

}
