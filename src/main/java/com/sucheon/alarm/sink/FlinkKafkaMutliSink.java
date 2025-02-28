package com.sucheon.alarm.sink;

import com.sucheon.alarm.event.RuleMatchResult;
import com.sucheon.alarm.event.alarm.AlarmMatchResult;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;

import java.util.Properties;

public class FlinkKafkaMutliSink<T> extends FlinkKafkaProducer<T> {

    //设置为kafka到下游精确一次投递
    public FlinkKafkaMutliSink(String defaultTopicId, KeyedSerializationSchema<T> serializationSchema, Properties producerConfig, FlinkKafkaPartitioner<T> customPartitioner) {
        super(defaultTopicId, serializationSchema, producerConfig, java.util.Optional.ofNullable(customPartitioner), Semantic.EXACTLY_ONCE, 5);
    }
}
