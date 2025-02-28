package com.sucheon.alarm.functions;

import com.sucheon.alarm.event.alarm.TemporaryAlarm;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;


/**
 * 临时告警函数
 */
public class TemporaryAlarmFunction extends ProcessFunction<TemporaryAlarm, TemporaryAlarm> {


    @Override
    public void processElement(TemporaryAlarm ruleMatchResult, ProcessFunction<TemporaryAlarm, TemporaryAlarm>.Context context, Collector<TemporaryAlarm> collector) throws Exception {
        collector.collect(ruleMatchResult);
    }
}
