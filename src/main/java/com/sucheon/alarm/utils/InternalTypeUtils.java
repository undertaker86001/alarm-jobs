package com.sucheon.alarm.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sucheon.alarm.config.ObjectSingleton;
import com.sucheon.alarm.constant.CommonConstant;
import com.sucheon.alarm.exception.DistrbuteException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * 内部数据转换处理类
 */
@Slf4j
public class InternalTypeUtils {


    public static <T> byte[] transferData(Logger logger, T pointData, List<String> fields){
        String result = "";
        try {
            result = ReflectUtils.appendSinkStr(pointData, fields);
        } catch (IOException | IllegalAccessException e) {

            //todo 记录点位数据异常和时间戳
            String errorMessage = ExceptionUtil.getErrorMessage(e);
            logger.error("转换数据成kafka可接受的数据格式异常: {}", errorMessage);
        }

        if (StringUtils.isNotBlank(result)) {
            //fury做序列化压缩, 提高性能
            return JsonConfigUtils.getSerializationInstance(result);
        }else {
            return new byte[0];
        }
    }

    /**
     * 根据字段名获取对象中对应的值
     * @param obj
     * @param fieldName
     * @return
     */
    public static Object getValueByFieldName(Object obj, String fieldName){
        Object value = ObjectSingleton.INSTANCE.getInstance();
        for (Field field: obj.getClass().getDeclaredFields()){
            field.setAccessible(true);
            try{
                if (field.getName().equals(fieldName)){
                    value = field.get(obj);
                    break;
                }
            } catch (IllegalAccessException e){
                log.error("判断当前对象中指定字段是否可以访问, 报错原因: \n", e);
            }
        }
        return value;
    }

    /**
     * 判断实例化的对象中是否所有字段都为空
     * @param obj
     * @return
     */
    public static boolean iaAllFieldsNull(Object obj){
        for (Field field: obj.getClass().getDeclaredFields()){
            field.setAccessible(true);
            try{
                if (field.get(obj) !=null){
                    return false;
                }
            } catch (IllegalAccessException e){
                log.error("判断当前对象中是否存在属性全都为空的现象, 报错原因: \n", e);
            }
        }
        return true;
    }

    /**
     * 将来源字段转换成string类型
     * @param logger
     * @param origin
     * @return
     */
    public static String getValueStr(Logger logger, Object origin){
        String value = "";
        try {
            value = CommonConstant.objectMapper.writeValueAsString(origin);
        } catch (JsonProcessingException e) {
            String errorMessage = ExceptionUtil.getErrorMessage(e);
            logger.error("当前来源字段转换成string类型失败， 原因为: {}", errorMessage);
        }
        return value;
    }

    /**
     * 根据来源对象获取指定字段名的值
     * @param logger
     * @param origin
     * @param fieldName
     * @return
     */
    public static String extractFieldValue(Logger logger, Object origin, String fieldName){
        Object changeAfterFieldValue = getValueByFieldName(origin, fieldName);
        String value = getValueStr(logger, changeAfterFieldValue);
        return value;

    }

    /**
     * 判断订阅的redis事件中data属性是否为空
     * @param obj
     * @return
     */
    public static boolean isPropertyNull(Object obj){
        for (Field field: obj.getClass().getDeclaredFields()){
            field.setAccessible(true);
            try{

                if (!field.getName().equals("data")){
                    continue;
                }else {
                    Object value = field.get(obj);
                    if (value==null){
                        return true;
                    }
                }
            } catch (IllegalAccessException e){
                log.error("解析失败, 当前对象不存在data属性，或者data属性为空\n", e);
            }
        }
        return false;
    }

    /**
     * 动态添加单个字段的值
     * @param obj
     * @param fieldName
     * @param fieldValue
     * @throws Exception
     */
    public static Object addField(Object obj, String fieldName, Object fieldValue) {
        try {
            Class<?> clazz = obj.getClass();
            Field field = clazz.getDeclaredField(fieldName);

            if (field == null) {
                // 如果字段不存在，则创建它
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true); // 设置字段为可访问

                // 修改字段的访问权限，使其可以被私有字段访问
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

                // 添加字段的值
                Field valueField = clazz.getDeclaredField("value");
                valueField.setAccessible(true);
                valueField.set(obj, fieldValue);

                // 通过反射获取字段的set方法并调用
                String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                Method setMethod = clazz.getDeclaredMethod(methodName, field.getType());
                setMethod.setAccessible(true);
                setMethod.invoke(obj, fieldValue);
                return obj;
            }
        } catch (Exception e){
            String errorMessage = ExceptionUtil.getErrorMessage(e);
            log.error("当前反射字段失败, 待添加的字段名： {}， 待添加的字段值: {}, \n 错误原因: {}", fieldName, fieldValue, errorMessage);
        }
        return obj;
    }

}
