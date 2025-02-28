package com.sucheon.alarm.event.alarm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 缓存从第一次过线到最后一次过线的时间
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AlarmMatchCacheInterval implements Serializable {

    /**
     * 第一次过线告警时间
     */
    private Long firstAlarmTime;

    /**
     * 最后一次过线告警时间
     */
    private Long lastAlarmTime;
}
