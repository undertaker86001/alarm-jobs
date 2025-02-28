package com.sucheon.alarm.mock.notify;

import com.sucheon.alarm.event.EventBean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class EventReceviverByInterrupt implements EventRecevier{

    public String threadName;

    private List<? extends EventBean> cacheQueue;

    ThreadLocal<List<? extends EventBean>> threadLocal;

    public EventReceviverByInterrupt(String threadName){
        cacheQueue = new ArrayList<>();
        threadLocal = new ThreadLocal<>();
        this.threadName = threadName;
    }

    @Override
    public List<? extends EventBean> currentBatchProcessData() {
        return threadLocal.get();
    }

    @Override
    public void update(List<? extends EventBean> message) {
        threadLocal.set(message);
    }

    @Override
    public String getThreadName(){
        return threadName;
    }


    @Override
    public CountDownLatch callbackCountDownLatch(CountDownLatch downLatch) {
        return downLatch;
    }
}
