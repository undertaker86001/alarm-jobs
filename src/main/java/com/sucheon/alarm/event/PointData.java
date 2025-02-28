package com.sucheon.alarm.event;

import com.sucheon.alarm.typeinfo.PointDataTypeInfoFactory;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.api.common.typeinfo.TypeInfo;

import java.time.LocalDate;
import java.util.List;


/**
 * 测点数据
 */
@TypeInfo(PointDataTypeInfoFactory.class)
@Getter
@Setter
public class PointData extends EventBean{

    /**
     * 该批次总包数
     */
    private Integer pageNum;


    /**
     * 上送的点位数据事件时间
     */
    private Long time;

    /**
     * 上报的设备采样时间，抹掉毫秒
     */
    private LocalDate deviceTime;

    /**
     * 低分分频特征值
     */
    private String bandSpectrum;

    /**
     * 均值
     */
    private Integer mean;


    /**
     * 高频均值
     */
    private Long meanHf;


    /**
     * 低分振动
     */
    private Integer meanLf;


    /**
     * 频段区间
     */
    private String peakFreqs;


    /**
     * 功率区间
     */
    private String peakPowers;


    /**
     * 标准差
     */
    private Integer std;

    /**
     * 转速
     */
    private String speed;

    /**
     * 振动原始数据
     */
    private String originalVibrate;


    /**
     * 健康度检测的状态值
     * 1.停机
     * 2.运转
     * 3.过渡
     * 4.脉冲
     * 5.异常
     * 6.其他
     * 7.卸载
     * 8.待机
     * 10.温度异常
     */
    private Integer status;

    /**
     * 高分摩擦
     */
    private String feature1;

    /**
     * 高分振动
     */
    private String feature2;


    /**
     * 高分功率
     */
    private String feature3;

    /**
     * 高分质量
     */
    private String feature4;

    /**
     * 自定义特征值
     */
    private String customFeature;

    /**
     * 算法包的版本号
     */
    private Integer version;

    /**
     * 温度
     */
    private Integer temperature;

    /**
     * 当前算法实例
     */
    private String algInstanceId;


    /**
     * 算法结果和报警事件，算法框架计算完的数据 (包含健康度数据和特征值数据)
     *
     *
     * "result_data":{//算法结果和算法报警事件，最终job sink出去的数据结构
     *     "alg_instance_id":"",//算法结果实例id
     *     "device_timestamp"：122345667,//时间戳
     *     "batch_id":"",//批次号
     *     "alg_result":{//算法结果
     *         "group_key1":{//分组
     *             "key1":"value1",//状态、健康度、振动特征值
     *         }
     *     }，
     *     "event":[{}]//算法报警事件
     * }
     */
    private String resultData;

    /**
     * 数据源 特征字段或算法结果字段
     * "feature":{//数据源 特征字段或算法结果字段
     *     "code1":{//数据树节点code
     *         "key1":"value1" //特征字段或算法结果key value
     *         }
     *     }
     */
    private String feature;

    /**
     * 算法间传递的数据，可以算法自定义或默认将alg_result
     *
     * "transfer_data":{
     *      "key1":"value1"// 算法自定义key value
     *     }
     * }
     */
    private String transferData;


}
