package com.sucheon.alarm.event.alarm;

import cn.hutool.json.JSONObject;
import com.ql.util.express.DefaultContext;

import java.util.List;

/**
 * 工况表达式计算方法
 */
public class BindObjectMethod {

    /**
     * 已经命中的工况集合
     */
    private List<String> oc;

    /**
     * 已经准备好的测点数据
     */
    private DefaultContext<String, Object> defaultContext;


    public BindObjectMethod(List<String> oc, DefaultContext<String, Object> defaultContext){
        this.oc = oc;
        this.defaultContext = defaultContext;
    }

    /**
     * 解析子表达式
     * @param code 数据树的节点编号
     * @param feature 特征值
     * @param workStatus 绑定的工况条件
     * @return
     */
    public String anyContains(String code, String feature, String workStatus) {
        if(oc.contains(workStatus)){
            Object value = defaultContext.get(feature);
            return String.valueOf(value);
        }
        return null;
    }

}

