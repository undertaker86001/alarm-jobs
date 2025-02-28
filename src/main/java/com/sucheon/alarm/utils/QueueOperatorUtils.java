package com.sucheon.alarm.utils;



import com.sucheon.alarm.event.alarm.AlarmContext;
import com.sucheon.alarm.queue.MemoryLimitedLinkedBlockingQueue;
import net.bytebuddy.agent.ByteBuddyAgent;

import java.lang.instrument.Instrumentation;

/**
 * 内存操作队列
 */
public class QueueOperatorUtils {

    private MemoryLimitedLinkedBlockingQueue<AlarmContext> memoryLimitedLinkedBlockingQueue;

    public QueueOperatorUtils() {
        ByteBuddyAgent.install();
        Instrumentation inst = ByteBuddyAgent.getInstrumentation();
        memoryLimitedLinkedBlockingQueue = new MemoryLimitedLinkedBlockingQueue<AlarmContext>(inst);
        memoryLimitedLinkedBlockingQueue.setMemoryLimit(Integer.MAX_VALUE);
    }

    public QueueOperatorUtils(long memorySize){
        ByteBuddyAgent.install();
        Instrumentation inst = ByteBuddyAgent.getInstrumentation();
        memoryLimitedLinkedBlockingQueue = new MemoryLimitedLinkedBlockingQueue<AlarmContext>(inst);
        memoryLimitedLinkedBlockingQueue.setMemoryLimit(memorySize);
    }


    public void offer(AlarmContext redisSubEvent){
        memoryLimitedLinkedBlockingQueue.offer(redisSubEvent);
    }

    public AlarmContext take() throws InterruptedException {
        return memoryLimitedLinkedBlockingQueue.take();
    }

    public MemoryLimitedLinkedBlockingQueue<AlarmContext> getMemoryLimitedLinkedBlockingQueue(){

        if (memoryLimitedLinkedBlockingQueue.getMemoryLimit() >=Integer.MAX_VALUE){
            memoryLimitedLinkedBlockingQueue.clear();
        }
        return memoryLimitedLinkedBlockingQueue;
    }

}
