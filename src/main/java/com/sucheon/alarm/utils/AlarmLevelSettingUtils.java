package com.sucheon.alarm.utils;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.jesse.l2cache.Cache;
import com.github.jesse.l2cache.CacheConfig;
import com.ql.util.express.ExpressRunner;
import com.sucheon.alarm.constant.CacheConstant;
import com.sucheon.alarm.constant.CommonConstant;
import com.sucheon.alarm.event.*;
import com.sucheon.alarm.event.alarm.AlarmExpressionObject;
import com.sucheon.alarm.event.alarm.AlarmMetric;
import com.sucheon.alarm.event.alarm.AlarmOcExpression;
import com.sucheon.alarm.event.oc.OcChangeObject;
import com.sucheon.alarm.event.oc.OcExpression;
import com.sucheon.alarm.event.oc.OcInstance;
import com.sucheon.alarm.event.oc.OcSubExpressionObject;
import com.sucheon.alarm.singleton.ExpressRunnerSingleton;
import com.sucheon.alarm.singleton.ParserUtilsSingleton;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sucheon.alarm.constant.CacheConstant.LEFT_EXPRESSION;
import static com.sucheon.alarm.constant.CacheConstant.RIGHT_EXPRESSION;
import static com.sucheon.alarm.customenum.MetricPriorityEnum.DATA_COLUMN;
import static com.sucheon.alarm.customenum.MetricPriorityEnum.OC_INSTANCE;

/**
 * 报警等级表达式转换工具类
 */
@NoArgsConstructor
public class AlarmLevelSettingUtils {

    private static CacheConfig cacheConfig;

    public AlarmLevelSettingUtils(CacheConfig cacheConfig){
        this.cacheConfig = cacheConfig;
    }

    /**
     * 工况表达式
     * @param ocChangeObject
     * @return 缓存<告警等级,<指标名称，对应的指标计算优先级权重>>
     */
    public static Map<String, AlarmMetric> transferAlarmLevel(OcChangeObject ocChangeObject) throws Exception {

        List<AlarmExpressionObject> alarmConditionList =  ocChangeObject.getAlarmLevelList();

        //----------------------------------------指标单一计算组织逻辑-----------------------------------------------------------


        //获取工况表达式
        List<OcInstance> ocInstanceList = ocChangeObject.getOcInstanceList();

        //工况指标计算<工况指标,<优先级权重,工况实例id>名称之间的映射关系
        Map<String, Map<Integer,Integer >> instanceMapping = new HashMap<>();

        //记录工况实例名称与工况表达式之间的映射关系
        Map<String, OcExpression> ocInstanceMapping = new HashMap<>();

        //维护工况实例名称与工况表达式之间的映射关系
        for (OcInstance ocInstance: ocInstanceList){
            for (OcExpression ocExpression: ocInstance.getOcExpressionList()){
                Map<Integer, Integer> instanceAndProperityMapping = new HashMap<>();
                instanceAndProperityMapping.put(OC_INSTANCE.getPriority(), ocInstance.getInstanceId());
                instanceMapping.put(ocExpression.getOcKey(), instanceAndProperityMapping);
                //设置工况实例id
                ocExpression.setOcInstanceId(ocInstance.getInstanceId());
                ocInstanceMapping.put(ocExpression.getOcKey(), ocExpression);
            }
        }

        //工况指标计算<工况指标,<优先级权重,工况实例id>
        // 根据收集的工况优先级依赖关系拆解
        Map<String, Map<Integer,Integer>> tempInstanceMapping = new HashMap<>();

        //过滤出表达式中包含的工况， 不包含的工况不参与计算
        Map<String, String> subExpressionByaAlarmLevel = new HashMap<>();
        //取出对应的多字段表达式
        for (AlarmExpressionObject alarmCondition: alarmConditionList){
            //当前告警等级的报警表达式
            String alarmLevel = alarmCondition.getAlarmLevel();
            String alarmThersoldExpression = alarmCondition.getAlarmThersoldExpression();
            //缓存当前告警等级对应的表达式
            subExpressionByaAlarmLevel.put(alarmLevel, alarmThersoldExpression);
            //计算当前工况表达式与设置的约束工况的依赖关系
            for (Map.Entry<String, Map<Integer, Integer>> item: instanceMapping.entrySet()){
                String ocKey = item.getKey();
                OcExpression ocExpression = ocInstanceMapping.get(ocKey);
                String ocExpr = ocExpression.getExpr();
                //匹配当前表达式中是否有出现工况相关的字段
                int res = KMPUtils.KMPSearch(alarmThersoldExpression, ocExpr);
                Map<Integer, Integer> ocInstanceWeight = item.getValue();
                if (res > -1){
                    tempInstanceMapping.put(ocKey, ocInstanceWeight);
                }

            }
        }


        //----------------------------------------指标依赖计算组织逻辑-----------------------------------------------------------

        //因为目前分工况的情况下，数据字段的计算是依赖原先工况的计算的, 所以最终给到计算逻辑的
        //应当是<告警指标,<告警表达式,优先级>> 的三元组
        //此处的value存的正是三元祖，而key代表归属于哪个告警等级
        Map<String, AlarmMetric> metricComputeProprityMapping = new HashMap<>();

        Map<String, Map<Integer,Integer>> cacheInstanceMapping = tempInstanceMapping;


        //记录哪个告警等级下的工况表达式组合
        int count = 0;
        for (Map.Entry<String, String> item: subExpressionByaAlarmLevel.entrySet()){
            //缓存每个告警等级的工况子表达式的token词法
            String alarmLevelExpression = item.getValue();
            String alarmLevel = item.getKey();


            for (Map.Entry<String, Map<Integer, Integer>> instance: cacheInstanceMapping.entrySet()){
                String metric = instance.getKey();
                //记录工况指标名称和对应工况表达式
                OcExpression ocExpression = ocInstanceMapping.get(metric);
                if (ocExpression != null){
                    AlarmMetric alarmMetric = new AlarmMetric();
                    alarmMetric.setMetricName(ocExpression.getOcKey());
                    alarmMetric.setMetricExpression(ocExpression.getExpr());
                    alarmMetric.setMetricPriority(OC_INSTANCE.getPriority());
                    metricComputeProprityMapping.put(alarmLevel, alarmMetric);
                }
            }




            if (!StringUtils.isEmpty(alarmLevelExpression)){
                AlarmMetric alarmMetric = new AlarmMetric();
                //当前告警等级下，工况表达式两两组合的name
                String alarmOcLevelOcCombineName = "alarm_level" + count;
                alarmMetric.setMetricName(alarmOcLevelOcCombineName);
                alarmMetric.setMetricExpression(alarmLevelExpression);
                alarmMetric.setMetricPriority(DATA_COLUMN.getPriority());
                count++;
                metricComputeProprityMapping.put(alarmLevel, alarmMetric);
            }

        }

        //1.输出计算工况表达式的优先级 2.以及指标间的依赖关系
        return metricComputeProprityMapping;
    }


    /**
     * 解析单个告警等级的工况表达式集合，解析成中缀表达式,
     * 方便后续组织成两两之间关联的子表达式对象
     * @param expression
     * @return
     */
    public static List<String> parseExpression(String expression) {
        List<String> result = new ArrayList<>();
        Deque<String> stack = new LinkedList<>();

        // 正则表达式匹配子表达式、比较操作符和运算符
        Pattern pattern = Pattern.compile("(\\w+\\(.*?\\) > \\d+)|(&&|\\|\\|)|\\(");
        Matcher matcher = pattern.matcher(expression);

        //按照括号优先级做工况子表达式的字符压栈
        while (matcher.find()) {
            String token = matcher.group();
            if (token.equals("&&") || token.equals("||")) {
                while (!stack.isEmpty() && (stack.peek().equals("&&") || stack.peek().equals("||"))) {
                    result.add(stack.pop());
                }
                stack.push(token);
            } else if (token.equals("(")) {
                stack.push(token);
            } else if (token.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    result.add(stack.pop());
                }
                stack.pop(); // 弹出左括号
            } else {
                result.add(token);
            }
        }


        // 将栈中剩余的元素弹出并添加到结果列表
        while (!stack.isEmpty()) {
            result.add(stack.pop());
        }

        return result;
    }

    /**
     * 解析每个工况的子表达式，比如 （比如 oc_key_field("xdsdsd-fsfsf_fsfd", "rms", "highspeed") > 100
     * && oc_key_field("xdsdsd-fsfsf_fsfd", "rms", "lowspeed ）> 29 )
     * @param ocSubExpressionObject 待解析的工况子表达式
     * @param newLeftOcSubExpressionObjectList 左边子表达式哪些内容需要记录到缓存
     * @param newRightOcSubExpressionObjectList 右边子表达式哪些内容需要记录到缓存
     * @throws Exception
     */
    public static OcDataColumnMetric analysisOcSubExpressionAndRecordCache(OcSubExpressionObject ocSubExpressionObject, List<AlarmOcExpression> newLeftOcSubExpressionObjectList, List<AlarmOcExpression> newRightOcSubExpressionObjectList) throws Exception {
        String leftExpression = ocSubExpressionObject.getLeftSubOcExpression();
        //通过设置缓存实例保存解析后的缓存上下文
        OcDataColumnMetric ocDataColumnMetric = new OcDataColumnMetric();
        //解析两两关联表达式的上下文
        extractOcSubExpressionContext(LEFT_EXPRESSION, leftExpression);
        Cache cache = TransferBeanutils.accessCurrentCacheInstance(CacheConstant.PARSER_DATA_CACHE_INSTANCE, cacheConfig);
        Object leftValue = cache.get(LEFT_EXPRESSION);
        List<AlarmOcExpression> leftAlarmOcExpressionList = CommonConstant.objectMapper.readValue(String.valueOf(leftValue), List.class);
        for (AlarmOcExpression alarmOcExpression: leftAlarmOcExpressionList) {
            AlarmOcExpression leftOcExpression =  assembleAlarmOcExpression(leftExpression, alarmOcExpression);
            ocDataColumnMetric.setLeftOcExpression(leftOcExpression);
        }
        //将工况表达式记录到本地缓存
        cache.put(LEFT_EXPRESSION, CommonConstant.objectMapper.writeValueAsString(newLeftOcSubExpressionObjectList));

        String rightExpression = ocSubExpressionObject.getRightSubOcExpression();
        //通过设置缓存实例保存解析后的缓存上下文
        extractOcSubExpressionContext(RIGHT_EXPRESSION, rightExpression);
        Object rightValue = cache.get(RIGHT_EXPRESSION);
        List<AlarmOcExpression> rightAlarmOcExpressionList = CommonConstant.objectMapper.readValue(String.valueOf(rightValue), List.class);
        for (AlarmOcExpression alarmOcExpression: rightAlarmOcExpressionList) {
            AlarmOcExpression rightOcExpression = assembleAlarmOcExpression(rightExpression, alarmOcExpression);
            ocDataColumnMetric.setRightOcExpression(rightOcExpression);
        }
        cache.put(RIGHT_EXPRESSION, CommonConstant.objectMapper.writeValueAsString(newRightOcSubExpressionObjectList));
        return ocDataColumnMetric;
    }

    /**
     * 提取工况子表达式需要解析的上下文
     * @param ocSubExpression 工况子表达式
     */
    public static void extractOcSubExpressionContext(String cacheKey, String ocSubExpression) throws Exception {
        ExpressRunner expressRunner = ExpressRunnerSingleton.INSTANCE.getInstance();

        ParserUtils parserUtils = ParserUtilsSingleton.INSTANCE.getInstance();
        parserUtils.setCacheKey(cacheKey);
        expressRunner.addFunctionOfServiceMethod("oc_field_value", parserUtils, "anyContains",
                new Class[]{String.class, String.class, String.class}, null);
        //根据当前告警等级， 计算出相关的表达式中工况相关的值，返回<数据树节点编号, 数据字段， 工况> 的三元组信息记录到本地缓存
        // 此处只是通过调用execute方法触发ParserUtils#anyContains的反射方法
        expressRunner.execute(ocSubExpression, null, null, false, false);
    }

    /**
     * 用于解析单个告警等级当中，每个工况子表达式的拆解方法
     * @param alarmThersoldExpression 当前工况子表达式
     * @param alarmOcExpression1 迭代中的工况子表达式
     * @return 解析完后的工况子表达式
     */
    public static AlarmOcExpression assembleAlarmOcExpression(String alarmThersoldExpression,  AlarmOcExpression alarmOcExpression1){

        String ocRuleName = alarmOcExpression1.getOcRuleName();
        int defineRes = KMPUtils.KMPSearch(alarmThersoldExpression, ocRuleName);
        int ocRuleNameExpression = defineRes + ocRuleName.length() + 2;

        int startOffset = ocRuleNameExpression + 1;
        int endOffset = ocRuleNameExpression + 2;

        String operator = alarmThersoldExpression.substring(startOffset +2, endOffset+3);
        String nextChar = alarmThersoldExpression.substring(startOffset, endOffset);

        int addOffset = 0;

        //计算出相关单一的工况表达式指标
        StringBuilder sb = new StringBuilder();
        while (!nextChar.equals("&&") &&  !nextChar.equals("||")){
            addOffset++;
            if (startOffset+addOffset > alarmThersoldExpression.length()-1){
                break;
            }
            sb.append(nextChar);
            nextChar = alarmThersoldExpression.substring(startOffset+addOffset, endOffset+addOffset);
        }

        alarmOcExpression1.setDataColumnOperator(operator);
        alarmOcExpression1.setDataColumnValue(sb.toString());
        return alarmOcExpression1;
    }

    /**
     * 将已经处理好的单告警等级中的工况子表达式集合
     * 通过双指针算法，组织成工况表达式对象 (目前优先级只能处理一级优先级,即一层括号的情况)
     *
     * @return
     */
    public static List<OcSubExpressionObject> assembleOcSubExpressionObjectList(List<String> tokenList){

        Deque<String> stack = new LinkedList<>();
        List<OcSubExpressionObject> ocSubExpressionObjectList = new ArrayList<>();
        //保留一个暂存的工况子表达式
        stack.push("undefined");
        //当前运算符是否更新的标记
        for (String token: tokenList){


            //监听更新的状态值，如果当前字符是token则用表达式判断
            //如果当前遍历当操作符，并且栈中元素只有两个 说明是第一次遍历表达式
            if (ExpressionParser.isOperator(token)&&stack.size()<3){

                //将之前记录的元素移除栈顶, 操作符压入栈
                OcSubExpressionObject ocSubExpressionObject = new OcSubExpressionObject();
                //对于第一次判断的场景
                if (stack.size()==2) {
                    String rightSubExpression = stack.peek();
                    stack.removeFirst();
                    String leftSubExpression = stack.getFirst();
                    stack.removeFirst();
                    //恢复右值表达式定义
                    stack.push(rightSubExpression);

                    ocSubExpressionObject.setLeftSubOcExpression(leftSubExpression);
                    ocSubExpressionObject.setRightSubOcExpression(rightSubExpression);
                    ocSubExpressionObject.setOperator(token);
                    ocSubExpressionObjectList.add(ocSubExpressionObject);
                }
                //将操作符压入栈 下次操作的时候再进行比较
                stack.push(token);
            }else if (!ExpressionParser.isOperator(token)){

                //边界条件判断，防止栈为空，出去空栈的情况
                if (stack.peek()!=null && stack.peek().equals("undefined")){
                    stack.pop();
                }

                //如果只有两个token子串的情况下, 只需要保留当前遍历到的子表达式
                if (stack.size() == 2){
                    //移除链表的头部
                    String leftOcSubExpression = stack.pop();
                    //移除操作符
                    String operator = stack.pop();
                    if (ExpressionParser.isOperator(leftOcSubExpression)){
                        stack.push(leftOcSubExpression);
                    }

                    if (ExpressionParser.isOperator(operator)){
                        stack.push(operator);
                    }

                }

                stack.push(token);

                //如果现在栈当中只有两个元素，此时应该只有一个子表达式和操作符
                if (stack.size() == 3){
                    OcSubExpressionObject ocSubExpressionObject = new OcSubExpressionObject();

                    String rightSubExpression =  stack.peek();
                    stack.pop();
                    String operator = stack.pop();
                    stack.pop();
                    String leftSubExpression = stack.pop();
                    stack.pop();
                    stack.push(rightSubExpression);
                    ocSubExpressionObject.setLeftSubOcExpression(leftSubExpression);
                    ocSubExpressionObject.setRightSubOcExpression(rightSubExpression);
                    ocSubExpressionObject.setOperator(operator);
                    ocSubExpressionObjectList.add(ocSubExpressionObject);
                }
//
            }

        }


        return ocSubExpressionObjectList;
    }

    private static boolean isOperator(String token) {
        return token.equals("&&") || token.equals("||");
    }

    private static int getPriority(String operator) {
        if (operator.equals("&&")) {
            return 2;
        } else if (operator.equals("||")) {
            return 1;
        } else {
            return 0;
        }
    }


    public static void main(String[] args) throws JsonProcessingException {
        String expression = "oc_key_field(\"tevdfg_xxxx_ddff\", \"rms\", \"yyyy\") > 10 ||" +
                " oc_key_field(\"tevdfg_xxxx_dd44\", \"rms\", \"yyyy\") > 12 " +
                "&& oc_key_field(\"tevdfg_xxxx_ddff\", \"rms\", \"yyyy\") > 10 " +
                "|| oc_key_field(\"tevdfg_xxxx_dd44\", \"rms\", \"yyyy\") > 45";
        List<String> expressions = parseExpression(expression);


        //[oc_key_field("tevdfg_xxxx_ddff", "rms", "yyyy") > 10,
        // oc_key_field("tevdfg_xxxx_dd44", "rms", "yyyy") > 12, ||,
        // oc_key_field("tevdfg_xxxx_ddff", "rms", "yyyy") > 10, &&,
        // oc_key_field("tevdfg_xxxx_dd44", "rms", "yyyy") > 45, ||]
        //双指针，根据工况表达式两两组合运算符
        System.out.println(expressions);


        List<OcSubExpressionObject> ocSubExpressionObjectList = assembleOcSubExpressionObjectList(expressions);
        System.out.println(CommonConstant.objectMapper.writeValueAsString(ocSubExpressionObjectList));
    }
}
