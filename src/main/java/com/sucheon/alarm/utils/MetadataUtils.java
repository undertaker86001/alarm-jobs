package com.sucheon.alarm.utils;

import com.sucheon.alarm.constant.CacheConstant;
import com.sucheon.alarm.constant.HttpConstant;
import com.sucheon.alarm.constant.RedisConstant;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.types.DataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sucheon.alarm.constant.CacheConstant.ALARM_TEMPORARY_CACHE_INSTANCE;

public class MetadataUtils {

    /**
     * 组装redis订阅和调用对应http接口更新事件
     * @return
     */
    public static Map<String, String> assembleChannelAndHttpUrl (){

        return new HashMap<String, String>() {
            { put(RedisConstant.ALARM_RULE, HttpConstant.alDataSourceUpdateHttpUrl);}
            { put(RedisConstant.ALG_OC_RULE, HttpConstant.algOcUpdateHttpUrl);}
            { put(RedisConstant.TERMINAL_POINT_TREE, HttpConstant.iotUpdateHttpUrl);}
        };
    }

    /**
     * 组装redis频道和对应缓存实例的映射关系
     * @return
     */
    public static Map<String, String> assembleChannelAndCacheInstance(){
        return new HashMap<String, String>() {
            { put(RedisConstant.ALARM_RULE , CacheConstant.ALARM_RULE_DATA_CACHE_INSTANCE); }
            { put(RedisConstant.ALG_OC_RULE , CacheConstant.ALARM_OC_DATA_CACHE_INSTANCE); }
            { put(ALARM_TEMPORARY_CACHE_INSTANCE, ALARM_TEMPORARY_CACHE_INSTANCE); }
            { put(RedisConstant.TERMINAL_POINT_TREE, CacheConstant.TERMINAL_POINT_TREE);}

        };
    }


    /**
     * 主标定链路需要过滤掉的数据字段
     * @return
     */
    public static List<String> assembleMainLabelFilterDataList(){
        List<String> filterData = new ArrayList<String>(){
            {add("topic");}
        };
        return filterData;
    }

    /**
     * 临时告警链路需要过滤掉的数据字段
     * @return
     */
    public static List<String> assembleTemporaryAlarmFilterDataList(){
        List<String> filterData = new ArrayList<String>(){
            {add("topic");}
            {add("overHeadCount");}
        };
        return filterData;
    }

    /**
     * 定义下游报警字段输出格式
     * @return
     */
    public static Map<String, DataType> assembleFieldNameAndDataType(){
        return new HashMap<String, DataType>(){
            {put("alg_group", DataTypes.STRING());}

            {put("point_id", DataTypes.STRING());}

            {put("device_channel", DataTypes.STRING());}

            {put("device_timestamp", DataTypes.STRING());}

            {put("batch_id", DataTypes.STRING());}

            {put("group_key", DataTypes.STRING());}

            {put("iot_json", DataTypes.STRING());}

            {put("alg_json", DataTypes.STRING());}


        };
    }

    /**
     * 内外网转换ip地址部署
     * @param uri
     * @return
     */
    public static String assembleHttpUrl(String uri, String port){
        return "http://" + uri + ":" + port;
    }



}
