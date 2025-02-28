package com.sucheon.alarm.functions;

import com.sucheon.alarm.event.RuleMatchResult;
import com.sucheon.alarm.event.alarm.AlarmMatchResult;
import com.sucheon.alarm.utils.InternalTypeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

import static com.sucheon.alarm.constant.CacheConstant.ALARM_TEMPORARY_CACHE_INSTANCE;

/**
 * 数据标定链路和临时告警链路关联逻辑（采用flink的state， redis两级缓存保证加载）
 * redis是为了防止state万一过期存在的
 */
public class AlarmViewConnectFunction extends CoProcessFunction<AlarmMatchResult, RuleMatchResult, AlarmMatchResult> {

    /**
     * 临时告警，缓存先到达的数据
     */
    private MapStateDescriptor<String, RuleMatchResult> temporaryAlarmDescriptor =
            new MapStateDescriptor<>(
                    "temporary-alarm",
                    TypeInformation.of(String.class),
                    TypeInformation.of(new TypeHint<RuleMatchResult>() {}));

    /**
     * 临时告警链路
     */
    private transient MapState<String, RuleMatchResult> temporaryAlarmState;


    @Override
    public void open(Configuration parameters) throws Exception {
       this.temporaryAlarmState = getRuntimeContext().getMapState(temporaryAlarmDescriptor);
       //默认state的过期时间为long的最大值
//       temporaryAlarmDescriptor.enableTimeToLive();
    }

    //数据标定链路
    @Override
    public void processElement1(AlarmMatchResult alarmMatchResult, CoProcessFunction<AlarmMatchResult, RuleMatchResult, AlarmMatchResult>.Context context, Collector<AlarmMatchResult> collector) throws Exception {
        RuleMatchResult temporaryAlarmResult = temporaryAlarmState.get(ALARM_TEMPORARY_CACHE_INSTANCE);
        if (temporaryAlarmResult == null){
            return;
        }

        String mainAlarmId = alarmMatchResult.getAlarmId();
        String sideAlarmId = temporaryAlarmResult.getAlarmId();
        if (InternalTypeUtils.iaAllFieldsNull(alarmMatchResult) || StringUtils.isBlank(alarmMatchResult.getAlarmId())){
            return;
        }

        if (mainAlarmId.equals(sideAlarmId)){
            collector.collect(alarmMatchResult);
        }

    }

    //临时告警链路
    @Override
    public void processElement2(RuleMatchResult ruleMatchResult, CoProcessFunction<AlarmMatchResult, RuleMatchResult, AlarmMatchResult>.Context context, Collector<AlarmMatchResult> collector) throws Exception {
        //缓存先达到的数据状态

        //todo 缓存新到达的缓存数据
        temporaryAlarmState.put(ALARM_TEMPORARY_CACHE_INSTANCE, ruleMatchResult);

    }

}
