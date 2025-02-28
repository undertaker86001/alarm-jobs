package com.sucheon.alarm.functions;

import com.alibaba.fastjson.JSON;
import com.sucheon.alarm.event.EventBean;
import com.sucheon.alarm.event.PointData;
import org.apache.flink.api.common.functions.MapFunction;

public class Json2EventBeanMapFunction implements MapFunction<String, EventBean> {


    @Override
    public PointData map(String value) throws Exception {

        PointData pointTree = null;
        try {
            pointTree = JSON.parseObject(value, PointData.class);
        } catch (Exception ex) {

        }
        return pointTree;
    }
}
