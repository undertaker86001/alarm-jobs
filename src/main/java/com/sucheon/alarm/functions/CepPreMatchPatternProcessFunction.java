package com.sucheon.alarm.functions;

import com.sucheon.alarm.constant.CommonConstant;
import com.sucheon.alarm.event.RuleMatchResult;
import com.sucheon.alarm.event.alarm.TemporaryAlarm;
import com.sucheon.alarm.utils.InternalTypeUtils;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.util.Collector;

import java.util.List;
import java.util.Map;

/**
 * cep预匹配处理规则
 */
public class CepPreMatchPatternProcessFunction extends PatternProcessFunction<TemporaryAlarm, TemporaryAlarm> {


    /**
     *
     * @param match：匹配上的数据,key就是start ， next， 这样每一个模式的名字, value就是匹配成功的数据
     * @param context: 上下文
     * @param collector: 向下游传递数据
     * @throws Exception
     */
    @Override
    public void processMatch(Map<String, List<TemporaryAlarm>> match, Context context, Collector<TemporaryAlarm> collector) throws Exception {


        /**
         * 发送匹配数据的Id:
         * 获取match中start的数据: match.get("start")得到的是一个list
         * .get(0)： 获取这个list中的第一条数据
         * */
        TemporaryAlarm exceptionData =  match.get(CommonConstant.cepAlertStep).get(0);
        if (exceptionData != null && InternalTypeUtils.iaAllFieldsNull(exceptionData)){
            exceptionData.setException(1);
            collector.collect(exceptionData);
        }

        TemporaryAlarm noExceptionData = match.get(CommonConstant.cepRecoverStep).get(0);
        if (noExceptionData != null && InternalTypeUtils.iaAllFieldsNull(noExceptionData)){
            noExceptionData.setException(0);
            collector.collect(noExceptionData);
        }

    }
}
