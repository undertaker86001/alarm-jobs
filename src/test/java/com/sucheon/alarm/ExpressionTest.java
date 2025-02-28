package com.sucheon.alarm;

import com.github.jesse.l2cache.Cache;
import com.github.jesse.l2cache.CacheConfig;
import com.ql.util.express.ExpressRunner;
import com.sucheon.alarm.constant.CacheConstant;
import com.sucheon.alarm.utils.TransferBeanutils;
import org.junit.Test;

import static com.sucheon.alarm.constant.CacheConstant.LEFT_EXPRESSION;
import static com.sucheon.alarm.utils.AlarmLevelSettingUtils.extractOcSubExpressionContext;

public class ExpressionTest {


    @Test
    public void testProprity(){

        String str = "a1=&b2>&|c4<&d6=&|";
        char[] chars = str.toCharArray();

        // 遍历字符数组，输出字符和对应的 ASCII 码
        for (char c : str.toCharArray()) {
            System.out.println("字符: " + c + ", ASCII码: " + c);
        }
    }


    @Test
    public void testExtractExpression() throws Exception {

        String expression = "oc_key_field(\"tevdfg_xxxx_ddff\", \"rms\", \"yyyy\") > 10 ||" +
                " oc_key_field(\"tevdfg_xxxx_dd44\", \"rms\", \"yyyy\") > 12 " +
                "&& oc_key_field(\"tevdfg_xxxx_ddff\", \"rms\", \"yyyy\") > 10 " +
                "|| oc_key_field(\"tevdfg_xxxx_dd44\", \"rms\", \"yyyy\") > 45";

        extractOcSubExpressionContext("currentNewCacheInstance",expression);
        CacheConfig cacheConfig = new CacheConfig();
        Cache cache = TransferBeanutils.accessCurrentCacheInstance("currentNewCacheInstance", cacheConfig);
        Object leftValue = cache.get(LEFT_EXPRESSION);
    }


}
