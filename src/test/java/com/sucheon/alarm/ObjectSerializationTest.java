package com.sucheon.alarm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sucheon.alarm.constant.CommonConstant;
import com.sucheon.alarm.event.alarm.AlarmExpressionObject;
import com.sucheon.alarm.event.oc.OcChangeObject;
import com.sucheon.alarm.event.oc.OcExpression;
import com.sucheon.alarm.event.oc.OcInstance;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ObjectSerializationTest {

    @Test
    public void testOcChangeObject() throws JsonProcessingException {
        OcChangeObject ocChangeObject = new OcChangeObject();

        List<AlarmExpressionObject> alarmExpressionObjectList = new ArrayList<>();

        AlarmExpressionObject alarmLevel1 = new AlarmExpressionObject();
        alarmLevel1.setAlarmKey("报警等级1");
        alarmLevel1.setAlarmThersoldExpression("oc_key_field(\"xddff_fff_vdff\", \"rms\", \"highspeed\")");
        alarmLevel1.setAlarmLevel("1");
        AlarmExpressionObject alarmLevel0 = new AlarmExpressionObject();
        alarmLevel0.setAlarmThersoldExpression("\" avgspeed > 5 && avgspeed < 9 && load = 0\"");
        alarmLevel0.setAlarmLevel("lowspeed");
        alarmLevel0.setAlarmKey("警告");

        alarmExpressionObjectList.add(alarmLevel0);
        alarmExpressionObjectList.add(alarmLevel1);
        ocChangeObject.setAlarmLevelList(alarmExpressionObjectList);
        ocChangeObject.setSpaceId(1234343);


        List<OcInstance> ocInstanceList = new ArrayList<>();
        List<OcExpression> ocExpressionList = new ArrayList<>();

        OcExpression ocExpression = new OcExpression();
        ocExpression.setExpr("\" avgspeed > 5 && avgspeed < 9 && load = 0\"");
        ocExpression.setOcInstanceId(12212121);
        ocExpressionList.add(ocExpression);

        OcExpression ocExpression1 = new OcExpression();
        ocExpression1.setExpr("\" avgspeed > 10  && load = 0\"");
        ocExpressionList.add(ocExpression1);

        OcInstance ocInstance1 = new OcInstance();
        ocInstance1.setInstanceId(1111);
        ocInstance1.setOcExpressionList(ocExpressionList);
        ocInstanceList.add(ocInstance1);

        ocChangeObject.setOcInstanceList(ocInstanceList);

        String result = CommonConstant.objectMapper.writeValueAsString(ocChangeObject);
        System.out.println(result);
    }

}
