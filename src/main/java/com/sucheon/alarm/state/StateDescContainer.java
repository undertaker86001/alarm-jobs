package com.sucheon.alarm.state;

import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;

public class StateDescContainer {

    /**
     * 广播需要订阅的规则
     */
    public static MapStateDescriptor<String, String> ruleStateDesc
            = new MapStateDescriptor<String, String>("rule_broadcast_state",
            TypeInformation.of(String.class),
            TypeInformation.of(new TypeHint<String>() {
                @Override
                public TypeInformation<String> getTypeInfo() {
                    return super.getTypeInfo();
                }
            }));

}
