package com.sucheon.alarm.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.fury.Fury;
import org.apache.fury.config.Language;

public class JsonConfigUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();


    public static ObjectMapper getObjectMapper(){
        //默认转驼峰
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true);
//        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
        return objectMapper;
    }

    public static <T> byte[] getSerializationInstance(T internalClass){
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(internalClass.getClass());
        byte[] result = fury.serialize(internalClass);
        return result;
    }
}
