package com.sucheon.alarm.singleton;

import com.sucheon.alarm.event.RuleMatchResult;
import com.sucheon.alarm.event.pointtree.PointTreeSpace;

import java.io.Serializable;

public enum PointTreeSpaceSingleton implements Serializable {
    INSTANCE;
    private PointTreeSpace pointTreeInstance;
    PointTreeSpaceSingleton(){
        pointTreeInstance = new PointTreeSpace();
    }

    public PointTreeSpace getInstance(){
        return pointTreeInstance;
    }
}
