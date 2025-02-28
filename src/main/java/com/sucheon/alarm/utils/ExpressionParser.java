package com.sucheon.alarm.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sucheon.alarm.event.ComputeRule;
import com.sucheon.alarm.event.ExpressionContext;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.sucheon.alarm.constant.ExpressionConstant.*;

/**
 * 从子表达式校验计算规则是否成立(按照token词法解析)
 */
public class ExpressionParser {


    private static final Map<String, Integer> priorityMap =new HashMap<String, Integer>(){
        { put("&&", 2); }
        { put("||", 1); }
        { put("undefined", 0); }
    };

    @Getter
    @Setter
    public static class LinkDefine{
        //当前node的值
        private String from;

        private String to;

        private Boolean visited = false;
    }

    @Getter
    @Setter
    public static class NodeDefine{
        //遍历的节点编号
        private Integer id;
        //节点名称
        private String name;

        private Integer level;

        private Boolean visited;

        private List<NodeDefine> adjustNodeList = new ArrayList<>();

        private List<LinkDefine> adjustLinkDefineList = new ArrayList<>();

    }


    enum NodeType { OPERATOR, VARIABLE, CONSTANT }

    public static NodeType getTypeByValue(String value){
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

    private static final Pattern pattern = Pattern.compile("(\\(([^()]+)\\))|([a-zA-Z0-9_]+)|([<>=]+)|(&&|\\|\\|)");


    public static boolean isOperator(String value) {
        // 定义支持的运算符集合
        Set<String> operators = new HashSet<>(Arrays.asList("&&", "||", ">", "<",  "&", "|", "^", "=", ">=", "<=", "!="));
        return operators.contains(value);
    }


    private static boolean isVariable(String value) {
        // 匹配以字母开头，后面可以跟字母、数字或下划线的字符串
        Pattern pattern = Pattern.compile("^[a-zA-Z][a-zA-Z_]*$");
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }

    public static List<ExpressionContext> parse(String expression) {
        List<ExpressionContext> result = new ArrayList<>();

        List<String> belongs =  new ArrayList<>();
        belongs.add("undefined");
        parseExpression(expression, result, belongs);
        return result;
    }

    private static void parseExpression(String expression, List<ExpressionContext> result, List<String> belong) {
        Matcher matcher = pattern.matcher(expression);
        while (matcher.find()) {
            String token = matcher.group();
            if (token.startsWith("(")) {
                // 遇到左括号，递归解析
                parseExpression(token.substring(1, token.length() - 1), result, new ArrayList<>(belong));
            } else if (priorityMap.containsKey(token)) {
                // 遇到操作符，更新belong
                belong.add(token);
                NodeType nodeType = getTypeByValue(token);
                ExpressionContext expressionContext = new ExpressionContext();
                expressionContext.setExpression(token);
                expressionContext.setBelongs(new ArrayList<>(belong));
                expressionContext.setPriority(priorityMap.getOrDefault(belong.get(belong.size() - 1), 0));
                expressionContext.setType(nodeType.name());
                result.add(expressionContext);
            } else {
                // 遇到变量或常量，创建子表达式
                NodeType nodeType = getTypeByValue(token);
                ExpressionContext expressionContext = new ExpressionContext();
                expressionContext.setExpression(token);
                expressionContext.setBelongs(new ArrayList<>(belong));
                expressionContext.setPriority(priorityMap.getOrDefault(belong.get(belong.size() - 1), 0));
                expressionContext.setType(nodeType.name());
                result.add(expressionContext);
            }
        }
    }

    public static List<ParserUtils.Operator> extractOperatorsWithBracketPriority(String expression) {
        List<ParserUtils.Operator> operators = new ArrayList<>();
        int bracketLevel = 0;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '(') {
                bracketLevel++;
            } else if (c == ')') {
                bracketLevel--;
            } else if (c == '&' && i + 1 < expression.length() && expression.charAt(i + 1) == '&') {
                operators.add(new ParserUtils.Operator("&&", bracketLevel));
                i++;
            } else if (c == '|' && i + 1 < expression.length() && expression.charAt(i + 1) == '|') {
                operators.add(new ParserUtils.Operator("||", bracketLevel));
                i++;
            }
        }

        return operators;
    }



    public static void main(String[] args) throws InterruptedException, JsonProcessingException {
        String expression = "( ( a >= 12 && v <= 23 ) || ( f >= 23 || e >= 343 ) ) || ( k >= 7 ) || ( ( h > 3 ) || (y < 23)  )";

        expression = "observered_normal_count>=30";

//        expression = " oc_field_key(\"txgggsg_gsgf_vsddg\", \"rms\", \"hightspeed\") > 10 ||  oc_field_key(\"txgggsg_gsgf_vsddg\", \"vcd\", \"lowspeed\")";
        List<ExpressionContext> result = parse(expression);
        System.out.println(JsonConfigUtils.getObjectMapper().writeValueAsString(result));
        List<ParserUtils.Operator> operatorList = extractOperatorsWithBracketPriority(expression);
        System.out.println(JsonConfigUtils.getObjectMapper().writeValueAsString(operatorList));

    }
}
