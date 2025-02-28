package com.sucheon.alarm.typeinfo;


import com.sucheon.alarm.event.PointData;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 将kafka接受到的pojo对象进行类型兼容
 */
public class PointDataTypeInfoFactory extends TypeInfoFactory<PointData> {
    @Override
    public TypeInformation<PointData> createTypeInfo(
            Type t, Map<String, TypeInformation<?>> genericParameters) {

        return Types.POJO(PointData.class, new HashMap<String, TypeInformation<?>>() { {
            put("pointId",Types.STRING);
            put("deviceChannel", Types.STRING);
            put("origin", Types.STRING);
            put("code", Types.STRING);
            put("topicList", Types.LIST(Types.GENERIC(String.class)));
            put("pageNum", Types.INT);
            put("batchId", Types.STRING);
            put("time", Types.LONG);
            put("deviceTimestamp", Types.LONG);
            put("deviceTime", Types.LOCAL_DATE);
            put("bandSpectrum", Types.STRING);
            put("mean", Types.INT);
            put("meanHf", Types.LONG);
            put("meanLf", Types.INT);
            put("peakFreqs", Types.STRING);
            put("peakPowers", Types.STRING);
            put("std", Types.INT);
            put("speed", Types.STRING);
            put("originalVibrate", Types.STRING);
            put("status", Types.INT);
            put("feature1", Types.STRING);
            put("feature2", Types.STRING);
            put("feature3", Types.STRING);
            put("feature4", Types.STRING);
            put("customFeature", Types.STRING);
            put("version", Types.INT);
            put("temperature", Types.INT);
            put("algInstanceId", Types.STRING);
            put("resultData", Types.STRING);
            put("feature", Types.STRING);
            put("transferData", Types.STRING);
        } } );
    }
}
