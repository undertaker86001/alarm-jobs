package com.sucheon.alarm.utils;

import com.github.jesse.l2cache.Cache;
import com.github.jesse.l2cache.CacheConfig;
import com.sucheon.alarm.constant.CommonConstant;
import com.sucheon.alarm.event.ExpressionContext;
import com.sucheon.alarm.event.RuleMatchResult;
import com.sucheon.alarm.event.alarm.TemporaryAlarm;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.sucheon.alarm.constant.CacheConstant.ALARM_TEMPORARY_CACHE_INSTANCE;

/**
 * 定义cep规则工具类
 * 1. 首先定义一个名为recover的Pattern，该Pattern的作用就是过滤出【正常观察计数】的数据，
 * 2. times(3) ,表示要匹配三次，也就是要三次【正常观察计数】.
 * 3. consecutive 表示上述的三次匹配要是连续的，比如 0, 0, 0，只有类似这样的数据才能被匹配到，中间不能有不符合的数据出现。
 * 4. followedBy表示该recover pattern的下面要跟着一个alert pattern，而followedBy是宽松匹配，也就是两个模式之间可以有其他的数据，如果要采用严格匹配，是使用next.
 * 5. alert后面再跟一个Pattern 叫recover匹配正常的计数
 * 6. 最后recover pattern加上一个optional 是为了区分报警，和报警恢复想的的一个方案，这样的话，如果是只匹配到了alert pattern，输出的就是报警，如果recovery pattern也匹配到了，那么就是报警恢复。
 *
 */
public class CepPatternUtils {


    private static final String PATTERN_MATCH_KEY = "patternMatchKey";

    /**
     * 连续过线计数多少次 一个或者多个正常
     * @param updateAlarmRuleInfo 从上游拉取报警规则变更的信息
     * @return
     */
    public static Pattern<TemporaryAlarm, TemporaryAlarm> buildAlarmPattern(CacheConfig cacheConfig, String updateAlarmRuleInfo){

        String temporaryAlarmExpression = "observered_normal_count=1";


        if (!StringUtils.isBlank(updateAlarmRuleInfo)){

//            AssembleDataUtil.matchAlarmRuleBySourceDataId()
            //todo 从报警规则中解析出临时告警的正常计数次数
            temporaryAlarmExpression = updateAlarmRuleInfo;
        }

        List<ExpressionContext> expressionContextList =  ExpressionParser.parse(temporaryAlarmExpression);

        ExpressionContext expressionVariable =  expressionContextList.get(0);
        ExpressionContext expressionOperator = expressionContextList.get(1);
        ExpressionContext expressionConstant = expressionContextList.get(2);

        String timeStr = expressionConstant.getExpression();
        int times = Integer.parseInt(timeStr);


        // 观察正常计数 从业务意义上考虑
        // 如果 observed_normal_count > 6, 意味着可以转observed_normal_count = 7 比超过单位高一级都满足了 更高的波形数据肯定满足
        // 如果 observed_normal_count < 5 意味着可以转observed_normal_count = 4 比低于单位低一级都满足了 更低的波形数据肯定满足
        String operator = expressionOperator.getExpression();
        if (operator.equals(">")){
            times = times + 1;
        }else if (operator.equals("<")){
            times = times - 1;
        }


        Cache cache =  CacheUtils.compositeCache(ALARM_TEMPORARY_CACHE_INSTANCE, cacheConfig);
        Object value = cache.get(PATTERN_MATCH_KEY);
        if (value == null){
            cache.put(PATTERN_MATCH_KEY, times);
            value = times;
        }

        int currentNormalTimes = Integer.parseInt(String.valueOf(value));




        Pattern<TemporaryAlarm, TemporaryAlarm> recover = Pattern.<TemporaryAlarm>begin(CommonConstant.cepRecoverStep)
                .where(new IterativeCondition<TemporaryAlarm>(){
            @Override
            public boolean filter(
                    TemporaryAlarm i, Context<TemporaryAlarm> context) throws Exception{
                return !i.getOverHeadCount();
            }
        }).times(currentNormalTimes);

        Pattern<TemporaryAlarm, TemporaryAlarm> alert =  recover.consecutive().followedBy(CommonConstant.cepAlertStep).where(new IterativeCondition<TemporaryAlarm>(){
            @Override
            public boolean filter(
                    TemporaryAlarm i, Context<TemporaryAlarm> context) throws Exception{
                return i.getOverHeadCount();
            }
        }).times(1);

        Pattern<TemporaryAlarm, TemporaryAlarm> finalMode = alert.consecutive().followedBy(CommonConstant.cepRecoverStep).where(new IterativeCondition<TemporaryAlarm>(){
            @Override
            public boolean filter(
                    TemporaryAlarm i,
                    Context<TemporaryAlarm> context) throws Exception{
                return !i.getOverHeadCount();
            }
        }).times(currentNormalTimes).optional();

        return finalMode;
    }
}
