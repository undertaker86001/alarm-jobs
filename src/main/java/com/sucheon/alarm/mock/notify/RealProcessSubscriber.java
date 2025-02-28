package com.sucheon.alarm.mock.notify;

import java.util.ArrayList;
import java.util.List;

/**
 * 测点数据类型
 * 用统配符号代替泛型，接受观察者通用事件
 */
public class RealProcessSubscriber implements ProcessSubscriber{

    private List<EventRecevier> recevierList = new ArrayList<>();



    @Override
    public List<EventRecevier> registerEventReceviver(EventRecevier eventReceviverByInterrupt) {
        recevierList.add(eventReceviverByInterrupt);
        return recevierList;
    }

    @Override
    public List<EventRecevier> removeEventReceviver(EventRecevier eventRecevier) {
        recevierList.remove(eventRecevier);
        return recevierList;
    }

    public List<EventRecevier> notifyEventReceiver() {
        return recevierList;
    }
}
