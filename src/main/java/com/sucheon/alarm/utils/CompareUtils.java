package com.sucheon.alarm.utils;

import com.sucheon.alarm.event.alarm.AlarmLevelMatchWatermark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class CompareUtils {

    /**
     * 比较各种类型的新值是否比当前旧值大
     * @param class1
     * @param class2
     * @return
     */
    public static Comparator<Object> getComparator(Class<?> class1, Class<?> class2) {
        if (class1 == class2) {
            if (Number.class.isAssignableFrom(class1)) {
                return Comparator.comparing(a -> BigDecimal.valueOf(((Number) a).doubleValue()));
            } else if (Comparable.class.isAssignableFrom(class1)) {
                return (a, b) -> ((Comparable) a).compareTo(b);
            } else if (class1.isArray()) {
                // 数组比较，递归调用
                return (a, b) -> compareArrays((Object[]) a, (Object[]) b);
            } else if (Collection.class.isAssignableFrom(class1)) {
                // 集合比较，递归调用，假设集合元素类型一致
                return (a, b) -> compareCollections((Collection<?>) a, (Collection<?>) b);
            }
        } else if (String.class.isAssignableFrom(class1) && String.class.isAssignableFrom(class2)) {
            return Comparator.comparing(String::valueOf);
        } else if (Date.class.isAssignableFrom(class1) && Date.class.isAssignableFrom(class2)
                || LocalDate.class.isAssignableFrom(class1) && LocalDate.class.isAssignableFrom(class2)
                || LocalDateTime.class.isAssignableFrom(class1) && LocalDateTime.class.isAssignableFrom(class2)) {
            // 日期比较，根据具体需求实现
            return (a, b) -> ((Comparable) a).compareTo(b);
        }

        // ... 其他类型比较器，例如自定义类，枚举等

        throw new IllegalArgumentException("Unsupported data type for comparison: " + class1 + ", " + class2);
    }


    private static int compareArrays(Object[] array1, Object[] array2) {
        // 数组比较，这里假设元素类型一致
        int minLength = Math.min(array1.length, array2.length);
        for (int i = 0; i < minLength; i++) {
            int result = compare(array1[i], array2[i]);
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(array1.length, array2.length);
    }

    private static int compareCollections(Collection<?> collection1, Collection<?> collection2) {
        // 集合比较，这里假设元素类型一致
        Iterator<?> iterator1 = collection1.iterator();
        Iterator<?> iterator2 = collection2.iterator();
        while (iterator1.hasNext() && iterator2.hasNext()) {
            int result = compare(iterator1.next(), iterator2.next());
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(collection1.size(), collection2.size());
    }

    // 递归调用 compare 方法
    private static int compare(Object o1, Object o2) {
        return getComparator(o1.getClass(), o2.getClass()).compare(o1, o2);
    }


    public static Comparator<? super Map.Entry<String, AlarmLevelMatchWatermark>> compare(Map.Entry<String, AlarmLevelMatchWatermark> before, Map.Entry<String, AlarmLevelMatchWatermark> after){

        return CompareUtils::compareAlarmLevel;
    }

    public static int compareAlarmLevel(Map.Entry<String, AlarmLevelMatchWatermark> before, Map.Entry<String, AlarmLevelMatchWatermark> after){
        //报警等级升序排序
        String lowLevel = before.getKey();
        String highLevel = after.getKey();

        Integer low = Integer.valueOf(lowLevel);
        Integer high = Integer.valueOf(highLevel);
        return high - low;
    }



    public static int compareAlarmTime(AlarmLevelMatchWatermark a1, AlarmLevelMatchWatermark a2){

        Long beforeFirstTime = a1.getFirstMatchAlarmTime();
        Long afterFirstTime = a2.getFirstMatchAlarmTime();

        //按照升序排序
        return afterFirstTime.compareTo(beforeFirstTime);
    }

}
