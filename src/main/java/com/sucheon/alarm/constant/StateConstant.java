package com.sucheon.alarm.constant;

import com.sucheon.alarm.event.alarm.AlarmLevelMatchWatermark;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;

public class StateConstant {

    /**
     * 订阅的当前需要恢复的事件(每个告警等级是否触发过线计数需要更新的state)
     */
    public static final ListStateDescriptor<AlarmLevelMatchWatermark> alarmLevelDescriptor =
            new ListStateDescriptor<>(
                    "buffered-elements",
                    TypeInformation.of(new TypeHint<AlarmLevelMatchWatermark>() {}));

    /**
     * 观察正常计数，待更新的计数变量
     */
    public static final ListStateDescriptor<Integer> observedNormalCountDescriptor =
            new ListStateDescriptor<>(
                    "observed-normal-count",
                    TypeInformation.of(new TypeHint<Integer>() {}));


    /**
     * 记录当前最新推送事件的时间戳
     */
    public static final ListStateDescriptor<Long> currentDeviceTimestampDescriptor =
            new ListStateDescriptor<Long>(
                    "current-device-timestamp",
                    TypeInformation.of(new TypeHint<Long>() {})
            );

    public static final ListStateDescriptor<String> currentPrevAlarmSymbolDescriptor =
            new ListStateDescriptor<String>(
                    "prev-alarm-symbol",
                    TypeInformation.of(new TypeHint<String>() {})
            );


}
