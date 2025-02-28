package com.sucheon.alarm;


import com.github.jesse.l2cache.CacheConfig;
import com.sucheon.alarm.config.CacheUtils;
import com.sucheon.alarm.config.HttpConf;
import com.sucheon.alarm.config.UserProperties;
import com.sucheon.alarm.constant.CacheConstant;
import com.sucheon.alarm.constant.HttpConstant;
import com.sucheon.alarm.constant.OutputTagConstant;
import com.sucheon.alarm.constant.RedisConstant;
import com.sucheon.alarm.event.NetworkConf;
import com.sucheon.alarm.event.RuleMatchResult;
import com.sucheon.alarm.event.alarm.AlarmMatchResult;
import com.sucheon.alarm.event.alarm.TemporaryAlarm;
import com.sucheon.alarm.functions.*;
import com.sucheon.alarm.partitioner.CustomRangePartitioner;
import com.sucheon.alarm.serializtion.CustomMainLabelOutputSerializationSchema;
import com.sucheon.alarm.serializtion.CustomTemporaryAlarmOutputSerializationSchema;
import com.sucheon.alarm.sink.FlinkKafkaMutliSink;
import com.sucheon.alarm.state.StateDescContainer;
import com.sucheon.alarm.utils.CepPatternUtils;
import com.sucheon.alarm.utils.TransferBeanutils;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.BroadcastConnectedStream;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Properties;


@Slf4j
@SpringBootApplication(exclude = {GsonAutoConfiguration.class, JacksonAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class})
public class AlarmMainJob {

    public static void main( String[] args ) throws Exception {

        System.setProperty("spring.devtools.restart.enabled", "false");
        ConfigurableApplicationContext applicationContext = SpringApplication.run(AlarmMainJob.class, args);

        UserProperties ddpsKafkaProperties = applicationContext.getBean(UserProperties.class);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", ddpsKafkaProperties.getBootStrapServers());
        properties.setProperty("auto.offset.reset", ddpsKafkaProperties.getAutoOffsetReset());
        properties.setProperty("fetch.max.bytes", ddpsKafkaProperties.getFetchMaxBytes());
        properties.setProperty("auto.create.topics", ddpsKafkaProperties.getAutoCreateTopics());
        // 构建env
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());
        env.setParallelism(1);

        CacheUtils cacheUtils = applicationContext.getBean(CacheUtils.class);
        CacheConfig cacheConfig = cacheUtils.getCacheConfig();

        // 读取kafka中的用户行为日志
        KafkaSourceBuilder kafkaSourceBuilder = new KafkaSourceBuilder();
        DataStream<String> dss = env.fromSource(kafkaSourceBuilder.newBuild(ddpsKafkaProperties.getAlarmSourceTopic(), ddpsKafkaProperties.getAlarmSourceGroup(), properties), WatermarkStrategy.noWatermarks(), "alarm-jobs");
        // json解析
        DataStream<String> dsBean = dss.filter(e -> e != null);

        NetworkConf networkConf = NetworkConf.builder().build();

        //订阅算法端配置
        DataStream<String> ruleBinlogs = env.addSource(new ConfPushDownSource(cacheConfig, networkConf));
        BroadcastStream<String> ruleBroadcast = ruleBinlogs.broadcast(StateDescContainer.ruleStateDesc);

        HttpConf httpConf = HttpConf.builder().exponential(ddpsKafkaProperties.getExponential())
                .retryTimes(ddpsKafkaProperties.getRetryTimes())
                .sleepTimeInMilliSecond(ddpsKafkaProperties.getSleepTimeInMilliSecond()).build();

        BroadcastConnectedStream<String, String> connect1 =  dsBean.connect(ruleBroadcast);


        SingleOutputStreamOperator<RuleMatchResult> withDynamicKey =  connect1.process(new AlarmRuleMatchFunction(cacheConfig, httpConf));

        //数据标定链路
        SingleOutputStreamOperator<AlarmMatchResult> alarmOverHeadFunction =  withDynamicKey.process(new AlarmOverheadCountFunction(cacheConfig, ddpsKafkaProperties.getSinkTopic(), ddpsKafkaProperties.getThreshold() ));


        FlinkKafkaMutliSink<AlarmMatchResult> mainLabelSink = new FlinkKafkaMutliSink<AlarmMatchResult>("main-label-topic", new CustomMainLabelOutputSerializationSchema(), properties, new CustomRangePartitioner<>());

        //数据标定链路输出到标定库
        alarmOverHeadFunction.addSink(mainLabelSink);

        //输出侧流到临时告警链路
        SingleOutputStreamOperator<TemporaryAlarm> temporaryAlarmFunction =  withDynamicKey.getSideOutput(OutputTagConstant.TEMPORARY_ALARM).process(new TemporaryAlarmFunction());


        //获取当前报警规则变更的信息
        String sendFinalResult = TransferBeanutils.assemblePreSendContext(HttpConstant.alDataSourceUpdateHttpUrl ,CacheConstant.ALARM_TEMPORARY_CACHE_INSTANCE, cacheConfig, null, false, null, null);

        //绑定cep告警规则，转化为临时告警算子
        PatternStream<TemporaryAlarm> cepMatchFunction =  CEP.pattern(temporaryAlarmFunction, CepPatternUtils.buildAlarmPattern(cacheConfig, sendFinalResult));
        SingleOutputStreamOperator<TemporaryAlarm> temporaryAlarmPreMatchFunction =   cepMatchFunction.process(new CepPreMatchPatternProcessFunction());


        //进入kafka的临时告警的sink
        FlinkKafkaMutliSink<TemporaryAlarm> temporaryAlarmSink = new FlinkKafkaMutliSink<TemporaryAlarm>("temporary-alarm-topic", new CustomTemporaryAlarmOutputSerializationSchema(), properties, new CustomRangePartitioner<>());

        //临时告警链路输出到临时告警库
        temporaryAlarmPreMatchFunction.addSink(temporaryAlarmSink);


        //实现数据标定和告警流的双流join
//        SingleOutputStreamOperator<AlarmMatchResult> coGroupOverHeadFunction =  alarmOverHeadFunction.connect(temporaryAlarmPreMatchFunction).process(new AlarmViewConnectFunction());

        //匹配的算法结果输出到kafka
//        coGroupOverHeadFunction.addSink(mainLabelSink);
        env.execute();
    }
}
