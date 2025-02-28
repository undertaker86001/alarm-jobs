package com.sucheon.alarm.event;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class NetworkConf implements Serializable {

    /**
     * 内外网转换地址
     */
    private String address;

    private String port;

}
