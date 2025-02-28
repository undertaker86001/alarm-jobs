package com.sucheon.alarm.utils;

import com.alibaba.fastjson.JSONObject;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.config.QLExpressRunStrategy;
import com.sucheon.alarm.event.alarm.BindObjectMethod;
import com.sucheon.alarm.singleton.ExpressRunnerSingleton;

import java.util.ArrayList;
import java.util.List;

public class ExpressionUtils {

    /**
     * 工况表达式是否成功计算
     * @param expression 待计算的工况表达式
     * @param pointData 待计算的测点数据
     * @param matchSuccessOcExpression 已经命中的工况以及对应的结果
     * @throws Exception
     */
    public static boolean computeOcExpression(String expression, DefaultContext<String, Object> pointData, List<String> matchSuccessOcExpression) throws Exception {

        ExpressRunner expressRunner = ExpressRunnerSingleton.INSTANCE.getInstance();;

        QLExpressRunStrategy.setCompareNullLessMoreAsFalse(true);
        BindObjectMethod bindObjectMethod = new BindObjectMethod(matchSuccessOcExpression, pointData);
        expressRunner.addFunctionOfServiceMethod("oc_field_value", bindObjectMethod, "anyContains",
                new Class[]{String.class, String.class, String.class}, null);

//        String express = "(oc_field_value('nodeId','name','highspeed')>10||oc_field_value('nodeId','name','lowspeed')>10)&&(oc_field_value('nodeId','name','highspeed')>10||oc_field_value('nodeId','name','lowspeed')>10)";

        Object o = expressRunner.execute(expression, pointData, null,false,true);
        boolean result = Boolean.parseBoolean(o.toString());
        return result;
    }
}
