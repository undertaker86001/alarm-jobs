package com.sucheon.alarm.functions;

import com.sucheon.alarm.constant.CommonConstant;
import com.sucheon.alarm.event.RuleMatchResult;
import com.sucheon.alarm.utils.ExceptionUtil;
import com.sucheon.alarm.utils.ReflectUtils;
import com.sucheon.alarm.utils.RowConverterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;
import org.apache.flink.types.RowUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

@Slf4j
public class FormatConvertRowFunction implements MapFunction<RuleMatchResult, Row> {

    @Override
    public Row map(RuleMatchResult ruleMatchResult) {
        Row row =null;
        try {
            row = RowConverterUtils.convertObject(ruleMatchResult);
            return row;
        }catch (Exception ex){
            String errorMessage = ExceptionUtil.getErrorMessage(ex);
            log.error("数据格式转换出错，请检查上游是否存在该字段，或者该字段是否有值!, 原因: {}", errorMessage);
        }
        return row;
    }

    public static void main(String[] args) throws Exception {
        RuleMatchResult ruleMatchResult = new RuleMatchResult();
        ruleMatchResult.setBatchId("11111");
        ruleMatchResult.setDeviceChannel("12121-21212-2121");
        ruleMatchResult.setPointId("111111");
        ruleMatchResult.setAlgJson("{ \"point_id\": 1111 }");
        ruleMatchResult.setIotJson(" \" alg_instance_id \": 12212 ");

        FormatConvertRowFunction formatConvertRowFunction = new FormatConvertRowFunction();
        Row row = formatConvertRowFunction.map(ruleMatchResult);

        System.out.println(CommonConstant.objectMapper.writeValueAsString(row.getFieldNames(true)));
    }
}
