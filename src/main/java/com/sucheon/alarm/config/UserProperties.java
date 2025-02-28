package com.sucheon.alarm.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;

@Configuration
@Data
public class UserProperties implements Serializable {

    private static final long serialVersionUID = 997809857078533653L;

    @Value("${ddps.kafka.bootstrap-servers}")
    private String bootStrapServers;

    @Value("${ddps.kafka.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${ddps.kafka.fetch-max-bytes}")
    private String fetchMaxBytes;

    @Value("${ddps.kafka.auto-create-topics}")
    private String autoCreateTopics;

    @Value("${ddps.kafka.alarm-source-topic}")
    private String alarmSourceTopic;

    @Value("${ddps.kafka.alarm-group-id}")
    private String alarmSourceGroup;

    /**
     * 重试时间
     */
    @Value("${ddps.http.retry-times:3}")
    private String retryTimes;

    /**
     * 运行失败后休眠对应时间再重试
     */
    @Value("${ddps.http.sleep-time-in-milli-second:20000}")
    private String sleepTimeInMilliSecond;

    /**
     * 休眠时间是否指数递增
     */
    @Value("${ddps.http.exponential:false}")
    private String exponential;

    /**
     * 第三方HTTP请求地址
     */
    @Value("${ddps.http.address:127.0.0.1}")
    private String address;

    /**
     * 第三方HTTP请求端口
     */
    @Value("${ddps.http.port:8090}")
    private String port;

    /**
     * 写入clickhouse地址
     */
    @Value("${ddps.clickhouse.address}")
    private String clickhouseAddress;

    /**
     * clickhouse数据库
     */
    @Value("${ddps.clickhouse.database}")
    private String clickhouseDatabase;

    /**
     * clickhouse数据表
     */
    @Value("${ddps.clickhouse.table}")
    private String clickhouseTable;

    /**
     * clickhouse用户名
     */
    @Value("${ddps.clickhouse.username}")
    private String clickhouseUserName;

    /**
     * clickhouse密码
     */
    @Value("${ddps.clickhouse.password}")
    private String clickhousePassWord;

    /**
     * clickhouse批次数据大小
     */
    @Value("${ddps.clickhouse.commit-batch-size:1}")
    private String clickhouseCommitBatchSize;

    /**
     * clickhouse每批数据重试时长
     */
    @Value("${ddps.clickhouse.commit-padding:2000}")
    private String clickhouseCommitPadding;


    /**
     * clickhouse每批数据重试次数
     */
    @Value("${ddps.clickhouse.commit-retry-attempts:3}")
    private String clickhouseCommitRetryAttempts;

    /**
     * clickhouse每批数据重试间隔
     */
    @Value("${ddps.clickhouse.commit-retry-interval:3000}")
    private String clickhouseCommitRetryInterval;

    /**
     * clickhouse每批数据提交遇到错误标志
     */
    @Value("${ddps.clickhouse.commit-ignore-error:false}")
    private String clickhouseCommitIgnoreError;

    /**
     * 写入到topic当中去
     */
    @Value("${ddps.kafka.sink-topic}")
    private String sinkTopic;

    /**
     * 一批数据攒多少才进行checkpoint快照
     */
    @Value("${ddps.checkpoint.threshold:300}")
    private Integer threshold;


    /**
     * 正常计数多少次(用于临时告警链路)
     */
    @Value("${ddps.alarm.over-head-times:10}")
    private Integer normalCountTimes;

}
