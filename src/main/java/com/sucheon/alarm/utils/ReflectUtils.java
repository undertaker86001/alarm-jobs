package com.sucheon.alarm.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sucheon.alarm.config.DynamicBean;
import com.sucheon.alarm.constant.CommonConstant;
import com.sucheon.alarm.event.PointData;
import com.sucheon.alarm.exception.DistrbuteException;
import com.sucheon.alarm.listener.context.IotNotStoreEventResult;
import org.apache.commons.beanutils.PropertyUtilsBean;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReflectUtils {


    private static Pattern humpPattern = Pattern.compile("[A-Z]");
    private static Pattern linePattern = Pattern.compile("\\_(\\w)");

    /**
     * 比较算法控制台配置的数据字段是否在边缘端数据中出现过
     * @param fieldNames
     * @param data
     * @return
     * @throws IllegalAccessException
     */
    public static Map<String, String> pointCompare(List<String> fieldNames, PointData data) throws IllegalAccessException {

        Class<?> clazz = data.getClass();

        Field[] fields = clazz.getDeclaredFields(); // 获取所有字段
        Map<String, Object> fieldMap = new HashMap<>();

        for (Field field : fields) {
            if (!field.isSynthetic()) { // 不处理合成字段（如编译器自动生成的）
                String name = field.getName(); // 获取字段名
                field.setAccessible(true);
                Object value = field.get(data);

                fieldMap.put(name, value); // 将字段名和描述存入 map
            }

        }
        Map<String, String> result = new HashMap<>();
        for (String fieldName: fieldNames) {
            if (!Objects.isNull(fieldMap.get(fieldName))){
                Object value = fieldMap.get(fieldName);
                result.put(fieldName, (String) value);
            }
        }

        return result;
    }

    public static <T> Map<String, Object> convertMap(T bean) throws IllegalAccessException {
        Class<?> clazz = bean.getClass();

        Field[] fields = clazz.getDeclaredFields(); // 获取所有字段
        Map<String, Object> fieldMap = new HashMap<>();

        for (Field field : fields) {
            if (!field.isSynthetic()) { // 不处理合成字段（如编译器自动生成的）
                String name = field.getName(); // 获取字段名
                field.setAccessible(true);
                Object value = field.get(bean);

                fieldMap.put(name, value); // 将字段名和描述存入 map
            }

        }
        return fieldMap;
    }


    public static <T> String appendSinkStr(T data, List<String> filterFields) throws IllegalAccessException, JsonProcessingException {
        Class<?> clazz = data.getClass();
        Field[] fields = clazz.getDeclaredFields(); // 获取所有字段
        Map<String, Object> fieldMap = new HashMap<>();
        for (int i =0; i< fields.length;i++) {
            Field field = fields[i];
            // 不处理合成字段（如编译器自动生成的）
            if (field.isSynthetic()){
                continue;
            }

            String name = humbleToCase(field); // 获取字段名

            //去除中间设置的topic数据
            if (filterFields.contains(name)){
                continue;
            }
            field.setAccessible(true);
            Object value = field.get(data);
            //由于边缘端的测点数据和算法侧的回传结果需要同时兼容，所以只传递有值的字段
            if (value != null) {
                fieldMap.put(name, value);
            }

        }
        String result = CommonConstant.objectMapper.writeValueAsString(fieldMap);
        return result;
    }


    /**
     * 如果JsonProperty有配置下划线，则替换
     * @param field
     * @return
     */
    public static String humbleToCase(Field field){
        Annotation[] annotations = field.getDeclaredAnnotations();
        for (Annotation annotation : annotations){
            String currentAnnotationName = annotation.annotationType().getSimpleName();
            if (!currentAnnotationName.equals("JsonProperty")){
                continue;
            }else {
                Matcher matcher = humpPattern.matcher(field.getName());
                StringBuffer sb = new StringBuffer();
                while (matcher.find()){
                    matcher.appendReplacement(sb, "\\_" + matcher.group(0).toLowerCase());
                }
                matcher.appendTail(sb);
                return sb.toString();
            }
        }
        return field.getName();
    }

    /**
     * 给对象动态添加属性
     * @param dest
     * @param addProperties
     * @return
     */
    public static Object getObject(Object dest, Map<String, Object> addProperties) {
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        PropertyDescriptor[] descriptors = propertyUtilsBean.getPropertyDescriptors(dest);
        Map<String, Class> propertyMap = new HashMap<>();
        for (PropertyDescriptor d : descriptors) {
            if (!"class".equalsIgnoreCase(d.getName())) {
                propertyMap.put(d.getName(), d.getPropertyType());
            }
        }
        addProperties.forEach((k, v) -> {
            String sclass = v.getClass().toString();
            if(sclass.equals("class java.util.Date")) {//对日期进行处理
                propertyMap.put(k, Long.class);
            }else {
                propertyMap.put(k, v.getClass());
            }

        });
        DynamicBean dynamicBean = new DynamicBean(dest.getClass(), propertyMap);
        propertyMap.forEach((k, v) -> {
            try {
                if (!addProperties.containsKey(k)) {
                    dynamicBean.setValue(k, propertyUtilsBean.getNestedProperty(dest, k));
                }
            } catch (Exception e) {
                System.out.println("动态添加字段出错:"+e);
            }
        });
        addProperties.forEach((k, v) -> {
            try {
                String sclass = v.getClass().toString();
                if(sclass.equals("class java.util.Date")) {//动态添加的字段为date类型需要进行处理
                    Date date = (Date) v;
                    dynamicBean.setValue(k, date.getTime());
                }else {
                    dynamicBean.setValue(k, v);
                }
            } catch (Exception e) {
                throw new DistrbuteException("动态添加字段出错:", e);
            }
        });
        Object obj = dynamicBean.getTarget();
        return obj;
    }


    /**
     * 根据指定的输入类动态添加属性
     * @param data
     * @param tClass
     * @param properties
     * @param <T>
     * @return
     * @throws JsonProcessingException
     */
    public static <T> T dynamicAddProperty(T data, Class<T> tClass, Map<String,Object> properties) throws IOException {
        Object changeAfterObject = ReflectUtils.getObject(data, properties);
        String changeObjectStr = CommonConstant.objectMapper.writeValueAsString(changeAfterObject);
        T result = CommonConstant.objectMapper.readValue(changeObjectStr, tClass);
        return result;
    }

    public static void main(String[] args) throws JsonProcessingException {
        Map<String, Object> properties =  new HashMap<>();
        properties.put("channel", "cheng");
        Object obj = ReflectUtils.getObject(new IotNotStoreEventResult(), properties);
        System.out.println(CommonConstant.objectMapper.writeValueAsString(obj));

    }





}
