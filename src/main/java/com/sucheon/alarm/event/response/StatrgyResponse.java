package com.sucheon.alarm.event.response;

import com.sucheon.alarm.event.alarm.StartgyCondition;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 策略响应相关的字段
 */
@Getter
@Setter
public class StatrgyResponse implements Serializable {

    /**
     * 数据树节点编号
     */
    private String code;

    /**
     * 策略条件
     */
    private StartgyCondition startgyCondition;
}
