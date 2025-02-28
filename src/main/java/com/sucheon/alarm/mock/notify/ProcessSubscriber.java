package com.sucheon.alarm.mock.notify;

import java.util.List;

public interface ProcessSubscriber{


    List<EventRecevier> registerEventReceviver(EventRecevier eventReceviverByInterrupt);


    List<EventRecevier> removeEventReceviver(EventRecevier eventRecevier);

}
