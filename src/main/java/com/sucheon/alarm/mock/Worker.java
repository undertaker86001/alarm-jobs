package com.sucheon.alarm.mock;

import com.sucheon.alarm.event.EventBean;
import com.sucheon.alarm.mock.notify.EventReceviverByInterrupt;
import com.sucheon.alarm.mock.notify.RealProcessSubscriber;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多线程顺序分发
 */
@Slf4j
public abstract class Worker implements Runnable{


    private CountDownLatch downLatch;

    //当前执行线程名字
    private String name;

    //统计当前线程执行到那个批次
    private Map<String, AtomicInteger> threadCountMapping = new HashMap<>();

    //每个线程的线程副本
    Map<String, ThreadLocal<List<? extends EventBean>>> threadLocalMap = new HashMap<>();

    private List<? extends EventBean> cacheQueue;


    private final Object lock = new Object();


    //  订阅事件
    private RealProcessSubscriber realProcessSubscriber;

    // 汇报事件产生进度
    private EventReceviverByInterrupt eventReceviverByInterrupt;

    public Worker(CountDownLatch downLatch, EventReceviverByInterrupt receviver, String name, ThreadLocal<List<? extends EventBean>> threadLocal){
        this.downLatch = downLatch;
        this.name = name;
        threadLocalMap.put(getName(), threadLocal);
        eventReceviverByInterrupt = new EventReceviverByInterrupt(name);
    }

    @Override
    public void run() {
        try {
            TimeUnit.SECONDS.sleep(new Random().nextInt(10));
        } catch (InterruptedException ie){
            this.downLatch.countDown();
        }


        this.doWork();

        //将线程的计数器推送给master
        eventReceviverByInterrupt.callbackCountDownLatch(downLatch);


        ThreadLocal<List<? extends EventBean>> currentThreadLocal = threadLocalMap.get(getName());
        if (currentThreadLocal !=null && currentThreadLocal.get()!=null) {
            synchronized (lock) {
                if (currentThreadLocal.get() != null) {
                    List<? extends EventBean> list = currentThreadLocal.get();
                    eventReceviverByInterrupt.update(list);
                }
            }
        }
    }

    public List<? extends EventBean> doWork() {
        ThreadLocal<List<? extends EventBean>> currentThreadLocal = threadLocalMap.get(getName());
        cacheQueue = callbackProcess(currentThreadLocal);
        threadLocalMap.put(getName(), currentThreadLocal);
        try {
            TimeUnit.SECONDS.sleep(new Random().nextInt(10));
        } catch (InterruptedException ie){

        }
        return currentThreadLocal.get();

    }

    public String getName() {
        return name;
    }

    public abstract String threadName();


    /**
     * 获取当前分片消费执行批次
     * 做为进度统计的失败编号
     * @return
     */
    public AtomicInteger getBatchByThreadName() {

        if (threadCountMapping.get(getName()) == null) {
            AtomicInteger atomicInteger = new AtomicInteger(0);
            threadCountMapping.put(getName(), atomicInteger);
        } else {
            AtomicInteger batchThreadCount = threadCountMapping.get(getName());
            batchThreadCount.incrementAndGet();
            threadCountMapping.put(getName(), batchThreadCount);
        }

        return threadCountMapping.get(getName());
    }


    public abstract List<? extends EventBean> callbackProcess(ThreadLocal<List<? extends EventBean>> currentThreadLocal);
}
