package com.sucheon.alarm.singleton;

import com.ql.util.express.ExpressRunner;

public enum ExpressRunnerSingleton {
    INSTANCE;
    private ExpressRunner parserInstance;
    ExpressRunnerSingleton(){
        parserInstance = new ExpressRunner();
    }

    public ExpressRunner getInstance(){
        return parserInstance;
    }
}
