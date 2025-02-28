package com.sucheon.alarm.mock.notify;


import com.sucheon.alarm.event.EventBean;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * worker线程事件接收器统计
 * (处理失败事件，命中点位树配置失败事件)
 */
public interface EventRecevier {

    public  void update(List<? extends EventBean> message);


    public List<? extends EventBean> currentBatchProcessData();

    public String getThreadName();

    /**
     * 用于推送的接口
     * 应该改造成threadLocal透传给master
     * 当前worker如果使用countdownlatch耗尽的时候
     * 应该把状态汇报给master
     * @return
     */
    public CountDownLatch callbackCountDownLatch(CountDownLatch downLatch);
}
