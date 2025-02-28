package com.sucheon.alarm.exception;

import java.io.IOException;

/**
 * IO抛出异常信息
 */
public class DistrbuteIOException extends IOException {

    public DistrbuteIOException(String message){
        super(message);
    }

    public DistrbuteIOException(String message, Throwable throwable){
        super(message,throwable);
    }

}
