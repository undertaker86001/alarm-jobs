package com.sucheon.alarm.constant;

public class HttpConstant {

    /**
     * 算法实例和工况之间的绑定关系
     *  1.只记录工况实例以及变更的对应的表达式信息
     */
    public static final String algOcUpdateHttpUrl = "/scpc/tms/PointTree/2342423422/update";


    /**
     * 告警规则和数据源
     *  1.工况和报警规则id的映射关系
     *  2.告警等级对应表达式，以及在哪个点位树空间下，
     *  3.以及sourceDataId(算法实例id)此时对应的工况是否发生变更
     *  4.收敛条件，重置策略，以及临时告警
     */
    public static final String alDataSourceUpdateHttpUrl = "";


    /**
     * 终端管理服务(点位树更新配置获取接口)
     */
    public static final String iotUpdateHttpUrl = "/scpc/tms/PointTree/2342423422/update";
}
