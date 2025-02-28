package com.sucheon.alarm.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataPoint implements Comparable<DataPoint>{

    private int value;
    private long timestamp;

    public DataPoint(int value, long timestamp){
        this.value = value;
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(DataPoint other) {
        return Long.compare(this.timestamp, other.getTimestamp());
    }
}
