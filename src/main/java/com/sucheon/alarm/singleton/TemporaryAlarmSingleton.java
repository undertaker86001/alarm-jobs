package com.sucheon.alarm.singleton;

import com.sucheon.alarm.event.alarm.TemporaryAlarm;

public enum TemporaryAlarmSingleton {
    INSTANCE;
    private TemporaryAlarm parserInstance;
    TemporaryAlarmSingleton(){
        parserInstance = new TemporaryAlarm();
    }

    public TemporaryAlarm getInstance(){
        return parserInstance;
    }
}
