package com.sucheon.alarm.constant;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RedisConstant {


    /**
     * 算法配置服务下发的工况
     */
    public static final String ALG_OC_RULE = "scpc:alg:oc";

    /**
     * 分发到告警的数据
     */
    public static final String ALARM_RULE = "scpc:alarm:rule";

    /**
     * 点位树变更的频道
     */
    public static final String TERMINAL_POINT_TREE = "scpc:tms:pointTree";


    public List<String> allChannelNames() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<String> allChannelNames = new ArrayList<>();
        Class<? extends RedisConstant> tClass = this.getClass();
        Object obj = tClass.getConstructor().newInstance();
        Field[] declaredFields = tClass.getDeclaredFields();
        //遍历字段
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            Object value = declaredField.get(obj);
            if (!Objects.isNull(value)) {
                allChannelNames.add(String.valueOf(value));
            }
        }
        return allChannelNames;
    }

}
