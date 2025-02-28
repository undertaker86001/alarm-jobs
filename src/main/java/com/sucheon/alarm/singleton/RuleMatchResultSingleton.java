package com.sucheon.alarm.singleton;

import com.sucheon.alarm.event.RuleMatchResult;

import java.io.Serializable;

public enum RuleMatchResultSingleton implements Serializable {

    INSTANCE;
    private RuleMatchResult ruleInstance;
    RuleMatchResultSingleton(){
        ruleInstance = new RuleMatchResult();
    }

    public RuleMatchResult getInstance(){
        return ruleInstance;
    }

}
