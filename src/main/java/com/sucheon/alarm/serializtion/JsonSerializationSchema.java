package com.sucheon.alarm.serializtion;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.util.function.SerializableSupplier;

public class JsonSerializationSchema<T> implements SerializationSchema<T> {

    private static final long serialVersionUID = 1L;

    private final SerializableSupplier<ObjectMapper> mapperFactory;

    protected transient ObjectMapper mapper;

    public JsonSerializationSchema() {
        this(() -> new ObjectMapper());
    }

    public JsonSerializationSchema(SerializableSupplier<ObjectMapper> mapperFactory) {
        this.mapperFactory = mapperFactory;
    }

    @Override
    public void open(InitializationContext context) {
        mapper = mapperFactory.get();
    }

    @Override
    public byte[] serialize(T element) {
        try {
            return mapper.writeValueAsBytes(element);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    String.format("Could not serialize value '%s'.", element), e);
        }

    }
}