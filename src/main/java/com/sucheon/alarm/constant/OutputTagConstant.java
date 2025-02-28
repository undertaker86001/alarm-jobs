package com.sucheon.alarm.constant;

import com.sucheon.alarm.event.RuleMatchResult;
import com.sucheon.alarm.event.alarm.TemporaryAlarm;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.OutputTag;

public class OutputTagConstant {

    /**
     * 临时告警测流
     */
    public static final OutputTag<TemporaryAlarm> TEMPORARY_ALARM = new OutputTag<>("temporary-alarm", TypeInformation.of(TemporaryAlarm.class));

}
