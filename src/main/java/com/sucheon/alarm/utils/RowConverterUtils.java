package com.sucheon.alarm.utils;

import com.sucheon.alarm.event.RuleMatchResult;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;
import org.apache.flink.types.RowUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

public class RowConverterUtils {


    public static Row convertObject(RuleMatchResult ruleMatchResult) throws IllegalAccessException {
        Class<?> clazz = ruleMatchResult.getClass();
        Field[] fields = clazz.getDeclaredFields(); // 获取所有字段

        Object[] values = new Object[fields.length-1];

        LinkedHashMap<String, Integer> positionByName = new LinkedHashMap<>();

        int j = 0;
        for (Field field : fields) {
            // 不处理合成字段（如编译器自动生成的）
            if (field.isSynthetic()) {
                continue;
            }
            String name = field.getName();
            positionByName.put(name, j);
            j++;
            field.setAccessible(true);
        }

        Row row = RowUtils.createRowWithNamedPositions(RowKind.INSERT, values, positionByName);

        for (Field field: fields){
            String name = field.getName();
            Object value = field.get(ruleMatchResult);
            row.setField(name, value);
        }

        return row;
    }
}
