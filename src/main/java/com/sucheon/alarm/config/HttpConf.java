package com.sucheon.alarm.config;

import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;

@Getter
@Builder
public class HttpConf implements Serializable {

    /**
     * 重试时间
     */
    private String retryTimes;

    /**
     * 运行失败后休眠对应时间再重试
     */
    private String sleepTimeInMilliSecond;

    /**
     * 休眠时间是否指数递增
     */
    private String exponential;
}
