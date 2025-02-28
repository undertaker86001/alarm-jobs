package com.sucheon.alarm.config;

/**
 * 基于枚举类实现的单例模式
 */
public enum ObjectSingleton {

    INSTANCE;
    private Object objectInstance;
    ObjectSingleton(){
        objectInstance = new Object();
    }

    public Object getInstance(){
        return objectInstance;
    }

}
