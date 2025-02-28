package com.sucheon.alarm.serializtion;

import com.sucheon.alarm.event.RuleMatchResult;
import com.sucheon.alarm.event.alarm.AlarmMatchResult;
import com.sucheon.alarm.event.alarm.TemporaryAlarm;
import com.sucheon.alarm.utils.InternalTypeUtils;
import com.sucheon.alarm.utils.MetadataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;

import java.util.List;

/**
 * 临时告警链路输出到下游序列化格式
 */
@Slf4j
public class CustomTemporaryAlarmOutputSerializationSchema implements KeyedSerializationSchema<TemporaryAlarm> {

    @Override
    public byte[] serializeKey(TemporaryAlarm ruleMatchResult) {

        List<String> filterData = MetadataUtils.assembleTemporaryAlarmFilterDataList();
        return InternalTypeUtils.transferData(log, ruleMatchResult, filterData);
    }

    @Override
    public byte[] serializeValue(TemporaryAlarm ruleMatchResult) {
        List<String> filterData = MetadataUtils.assembleTemporaryAlarmFilterDataList();
        return InternalTypeUtils.transferData(log, ruleMatchResult, filterData);
    }

    @Override
    public String getTargetTopic(TemporaryAlarm ruleMatchResult) {

        if (ruleMatchResult == null) {
            return "default-topic";
        }
        return ruleMatchResult.getTopic();
    }
}
