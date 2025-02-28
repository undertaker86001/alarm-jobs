package com.sucheon.alarm.mock;

import com.alibaba.fastjson.JSON;

import com.sucheon.alarm.event.EventBean;
import com.sucheon.alarm.mock.notify.EventRecevier;
import com.sucheon.alarm.mock.notify.EventReceviverByInterrupt;
import com.sucheon.alarm.mock.notify.RealProcessSubscriber;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 需要基于观察者模式做进度统计
 */
@Slf4j
public class Master implements Runnable{

    private CountDownLatch downLatch;


    private RealProcessSubscriber subscriber;

    private Map<String, EventReceviverByInterrupt> eventReceviverByInterruptMap;

    public Master(CountDownLatch downLatch, Map<String, EventReceviverByInterrupt> eventReceviverByInterruptMap){
        this.downLatch = downLatch;
        subscriber = new RealProcessSubscriber();

        for (Map.Entry<String, EventReceviverByInterrupt> eventRecevier: eventReceviverByInterruptMap.entrySet()){
            EventReceviverByInterrupt eventReceviverByInterrupt = eventRecevier.getValue();
            subscriber.registerEventReceviver(eventReceviverByInterrupt);
        }

        this.eventReceviverByInterruptMap = eventReceviverByInterruptMap;



    }


    @Override
    public void run() {


        List<EventRecevier> childNodeProcess = subscriber.notifyEventReceiver();

        for (EventRecevier eventRecevier: childNodeProcess) {
            List<? extends EventBean> eventBeanList = eventRecevier.currentBatchProcessData();
            log.info(String.format("当前线程%s: 处理数据: %s", eventRecevier.getThreadName(), JSON.toJSONString(eventBeanList)));
            //todo 处理失败事件 打印 or 落盘?
        }

        //FIXME 如果线程减到0了 重新设置阻塞线程
        long currentThreadCount = this.downLatch.getCount();
        if (currentThreadCount ==0L){
            downLatch = new CountDownLatch(10);
        }

        try {
            this.downLatch.await();
        } catch (InterruptedException e){
            e.getCause().getMessage();
        }

    }
}
