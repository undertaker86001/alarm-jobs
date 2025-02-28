package com.sucheon.alarm.constant;

/**
 * 配置映射对应频道的redis缓存实例名称
 */

public class CacheConstant {
    /**
     * 告警规则数据缓存实例
     */
    public static final String ALARM_RULE_DATA_CACHE_INSTANCE = "alarmRuleData";

    /**
     * 告警工况数据缓存实例
     */
    public static final String ALARM_OC_DATA_CACHE_INSTANCE = "alarmOcData";

    /**
     * 终端管理服务点位树变更
     */
    public static final String TERMINAL_POINT_TREE = "terminalPointTree";

    /**
     * 可解析的数据
     */
    public static final String PARSER_DATA_CACHE_INSTANCE = "parserData";

    /**
     * 左表达式缓存key
     */
    public static final String LEFT_EXPRESSION = "leftSubExpression";


    /**
     * 右表达式缓存key
     */
    public static final String RIGHT_EXPRESSION = "rightSubExpression";


    /**
     * 过线计数命中成功结果的缓存实例
     */
    public static final String ALARM_MATCH_SUCCESS_INSTANCE = "alarmCountCacheInstance";

    /**
     * 临时告警缓存实例
     */
    public static final String ALARM_TEMPORARY_CACHE_INSTANCE = "temporaryAlarmCacheInstance";



}
