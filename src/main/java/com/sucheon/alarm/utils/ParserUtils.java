package com.sucheon.alarm.utils;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.jesse.l2cache.Cache;
import com.github.jesse.l2cache.CacheConfig;
import com.sucheon.alarm.constant.CacheConstant;
import com.sucheon.alarm.constant.CommonConstant;
import com.sucheon.alarm.event.alarm.AlarmOcExpression;
import com.sucheon.alarm.event.ComputeRule;
import com.sucheon.alarm.event.alarm.SubExpression;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 从子表达式校验计算规则是否成立(按照逆波兰式解析)
 */
@NoArgsConstructor
@Setter
@Getter
public class ParserUtils {

    private CacheConfig cacheConfig;

    /**
     * 当前指定的工况子表达式实例
     */
    private String cacheKey;


    public ParserUtils(CacheConfig cacheConfig){
        this.cacheConfig = cacheConfig;
    }

    public ParserUtils(String cacheInstance){
        this.cacheKey = cacheInstance;
    }

    enum NodeType { OPERATOR, VARIABLE, CONSTANT }

    @Getter
    @Setter
    public static class Node {
        private String value;
        private NodeType type;
        List<Node> children = new ArrayList<>();
        List<ComputeRule> computeRuleList = new ArrayList<>();

        public Node(String value) {
            this.value = value;
            if (isOperator(value)) {
                type = NodeType.OPERATOR;
            } else if (isVariable(value)) {
                type = NodeType.VARIABLE;
            } else {
                type = NodeType.CONSTANT;
            }
        }

        public NodeType getTypeByValue(String value){
            NodeType type;
            if (isOperator(value)) {
                type = NodeType.OPERATOR;
            } else if (isVariable(value)) {
                type = NodeType.VARIABLE;
            } else {
                type = NodeType.CONSTANT;
            }
            return type;
        }

        public BigDecimal calculate(Map<String, BigDecimal> variables) {

            switch (type) {
                case OPERATOR:
                    switch (value) {
                        //todo 预留子表达式计算
                        case "&":
                        case "|":
                        // ... 其他运算符
                        case ">":
                        case "<":
                        case "^":
                        case "=":
                    }
                    break;
                case VARIABLE:
                    return variables.getOrDefault(value, new BigDecimal(0)); // 默认值为false
                case CONSTANT:
                    return BigDecimal.valueOf(Long.parseLong(value));
            }
            return new BigDecimal(0); // 默认返回false
        }

        private boolean isOperator(String value) {
            // 定义支持的运算符集合
            Set<String> operators = new HashSet<>(Arrays.asList("&&", "||", ">", "<",  "&", "|", "^", "=", ">=", "<=", "!="));
            return operators.contains(value);
        }
    }

    @Getter
    public static class Operator{
        private String op;
        private int priority;

        public Operator(String op, int priority) {
            this.op = op;
            this.priority = priority;
        }
    }

    public static class ExpressionParser {
        private static Map<Character, Integer> precedence = new HashMap<>();
        static {
            precedence.put('(', 0);
            precedence.put(')', 0);
            precedence.put('|', 1);
            precedence.put('^', 2);
            precedence.put('&', 3);
            precedence.put('<', 4);
            precedence.put('>', 4);
            precedence.put('=', 4);
            precedence.put('!', 4);
        }

        public static Node parse(String infixExpression, AtomicInteger count, List<ComputeRule> computeRuleList) {
            String postfixExpression = infixToPostfix(infixExpression);
            return postfixToAST(postfixExpression, count, computeRuleList);
        }

        public static String infixToPostfix(String infix) {
            Deque<Character> operatorStack = new LinkedList<>();
            StringBuilder postfix = new StringBuilder();

            for (char c : infix.toCharArray()) {

                if (c == ' '){
                    continue;
                }
                if (Character.isLetterOrDigit(c)) {
                    postfix.append(c);
                } else if (c == '(') {
                    operatorStack.push(c);
                } else if (c == ')') {

                    while (!operatorStack.isEmpty() && operatorStack.peek() != '(') {
                        postfix.append(operatorStack.pop());
                    }

                    operatorStack.pop(); // 弹出左括号

                } else {
                    while (!operatorStack.isEmpty() && precedence.get(c) <= precedence.get(operatorStack.peek())) {
                        postfix.append(operatorStack.pop());
                    }
                    operatorStack.push(c);
                }
            }

            while (!operatorStack.isEmpty()) {
                postfix.append(operatorStack.pop());
            }

            return postfix.toString();
        }


        public static Node postfixToAST(String postfix, AtomicInteger count, List<ComputeRule> computeRuleList) {
            Deque<Node> stack = new LinkedList<>();
            //todo 去除字符后面的acsiall码
            for (char c : postfix.toCharArray()) {
                if (Character.isLetterOrDigit(c)) {
                    stack.push(new Node(String.valueOf(c)));
                } else {
                    //后缀数组转中缀遇到比较符号跳过
                    Node stackTop = stack.peek();
                    if (stackTop != null) {
                        String symbol = stackTop.getValue();
                        Character character = symbol.toCharArray()[0];
                        if (precedence.get(character)!=null){
                            continue;
                        }
                    }

                    Node right = stack.pop();
                    Node left = stack.pop();
                    Node node = new Node(String.valueOf(c));
                    node.children.add(left);
                    node.children.add(right);

                    ComputeRule computeRule = new ComputeRule();
                    SubExpression subExpression = new SubExpression();
                    subExpression.setVariable(node.children.get(0).getValue());
                    subExpression.setConstant(node.children.get(1).getValue());
                    subExpression.setOperator(node.value);
                    computeRule.setComputeRule(subExpression);
                    count.incrementAndGet();
                    computeRule.setProprioty(count.get());
                    computeRule.setComputeRule(subExpression);
                    computeRuleList.add(computeRule);

                    stack.push(node);
                }
            }
            return stack.pop();
        }

        public static BigDecimal evaluate(Node root, Map<String, BigDecimal> variables, AtomicInteger count, List<ComputeRule> computeRuleList) {
            return root.calculate(variables);
        }

    }

    private static boolean isVariable(String value) {
        // 匹配以字母开头，后面可以跟字母、数字或下划线的字符串
        Pattern pattern = Pattern.compile("^[a-zA-Z][a-zA-Z_]*$");
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }

    public static List<Operator> extractOperatorsWithBracketPriority(String expression) {
        List<Operator> operators = new ArrayList<>();
        int bracketLevel = 0;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '(') {
                bracketLevel++;
            } else if (c == ')') {
                bracketLevel--;
            } else if (c == '&' && i + 1 < expression.length() && expression.charAt(i + 1) == '&') {
                operators.add(new Operator("&&", bracketLevel));
                i++;
            } else if (c == '|' && i + 1 < expression.length() && expression.charAt(i + 1) == '|') {
                operators.add(new Operator("||", bracketLevel));
                i++;
            }
        }

        return operators;
    }

    /**
     * 缓存到工况实例
     * @param code 绑定的数据树节点编号
     * @param expr1 当前用户选择的数据字段
     * @param expr2 选择的工况实例
     */
    public void anyContains(String code, String expr1, String expr2) throws JsonProcessingException {

        List<AlarmOcExpression> alarmOcExpressionList = new ArrayList<>();
        Cache cache = TransferBeanutils.accessCurrentCacheInstance(CacheConstant.PARSER_DATA_CACHE_INSTANCE, this.cacheConfig);


        if (cache.get(CommonConstant.cacheKey) != null){
            Object value = cache.get(CommonConstant.cacheKey);
            List<Object> currrentCacheList =  CommonConstant.objectMapper.readValue(String.valueOf(value), List.class);
            alarmOcExpressionList = currrentCacheList.stream().map(x -> {
                if (x!=null) {
                    try {
                        return CommonConstant.objectMapper.readValue(String.valueOf(x), AlarmOcExpression.class);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                return new AlarmOcExpression();
            }).collect(Collectors.toList());
            alarmOcExpressionList.add(alarmOcExpression(code, expr1, expr2));
            cache.put(cacheKey,CommonConstant.objectMapper.writeValueAsString(alarmOcExpressionList));
        }else {
            List<AlarmOcExpression> alarmOcExpressionList1 = new ArrayList<>();
            alarmOcExpressionList1.add(alarmOcExpression(code, expr1, expr2));
            cache.put(cacheKey,CommonConstant.objectMapper.writeValueAsString(alarmOcExpressionList1));
        }


    }

    public static void main(String[] args) {
        String expression = "( ( a > 12 && v < 23 ) || ( f > 23 || e > 343 ) ) || ( k > 7 )";
//        expression = " oc_field_value(\"tree1-cj1-bj1-dj1-A_HDE_H\", \"rms\", \"highSpeed\") ";
        Map<String, BigDecimal> variables = new HashMap<>();
        variables.put("a", new BigDecimal(0));
        variables.put("b", new BigDecimal(0));
        variables.put("c", new BigDecimal(0));
        variables.put("d", new BigDecimal(0));

//        List<Operator> operatorList =  extractOperatorsWithBracketPriority(expression);
//        System.out.println(JSON.toJSONString(operatorList));

        AtomicInteger count = new AtomicInteger(0);
        List<ComputeRule> computeRuleList = new ArrayList<>();
        Node root = ExpressionParser.parse(expression, count, computeRuleList);
        System.out.println(JSON.toJSONString(computeRuleList));
        BigDecimal result = ExpressionParser.evaluate(root, variables, count,  computeRuleList);
        System.out.println(result);
        // [{"computeRule":{"constant":"1","operator":"=","variable":"a"},"proprioty":1},{"computeRule":{"constant":"2","operator":">","variable":"b"},"proprioty":2},{"computeRule":{"constant":"4","operator":"<","variable":"c"},"proprioty":3},{"computeRule":{"constant":"6","operator":"=","variable":"d"},"proprioty":4}]
        System.out.println(JSON.toJSONString(computeRuleList));



    }


    public AlarmOcExpression alarmOcExpression(String code, String expr1, String expr2){
        AlarmOcExpression alarmOcExpression = new AlarmOcExpression();
        alarmOcExpression.setCode(code);
        alarmOcExpression.setDataColumn(expr1);
        alarmOcExpression.setOcRuleName(expr2);
        return alarmOcExpression;
    }
}
