package com.sucheon.alarm.singleton;

import com.sucheon.alarm.utils.ParserUtils;

/**
 * 解析类单例，避免在调用ParserUtils反射方法的时候过多实例化
 */
public enum ParserUtilsSingleton{
    INSTANCE;
    private ParserUtils parserInstance;
    ParserUtilsSingleton(){
        parserInstance = new ParserUtils();
    }

    public ParserUtils getInstance(){
        return parserInstance;
    }
}
