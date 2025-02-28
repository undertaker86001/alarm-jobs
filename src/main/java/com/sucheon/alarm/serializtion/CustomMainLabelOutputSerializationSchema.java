package com.sucheon.alarm.serializtion;

import com.sucheon.alarm.event.alarm.AlarmMatchResult;
import com.sucheon.alarm.utils.InternalTypeUtils;
import com.sucheon.alarm.utils.MetadataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;

import java.util.ArrayList;
import java.util.List;


/**
 * 主数据标定链路输出到kafka的序列化格式
 */
@Slf4j
public class CustomMainLabelOutputSerializationSchema implements KeyedSerializationSchema<AlarmMatchResult> {

    @Override
    public byte[] serializeKey(AlarmMatchResult data) {

        List<String> fields = MetadataUtils.assembleMainLabelFilterDataList();

        return InternalTypeUtils.transferData(log, data, fields);
    }

    @Override
    public byte[] serializeValue(AlarmMatchResult data) {
        List<String> fields = MetadataUtils.assembleMainLabelFilterDataList();
        return InternalTypeUtils.transferData(log, data, fields);
    }

    @Override
    public String getTargetTopic(AlarmMatchResult algResult) {
        if (algResult == null) {
            return "default-topic";
        }

        return algResult.getTopic();
    }
}
