package com.sucheon.alarm.exception;

/**
 * 定义非受检异常
 */
public class DistrbuteException extends RuntimeException{


    public DistrbuteException(String message){
        super(message);
    }

    public DistrbuteException(String message, Throwable throwable){
        super(message,throwable);
    }

}
