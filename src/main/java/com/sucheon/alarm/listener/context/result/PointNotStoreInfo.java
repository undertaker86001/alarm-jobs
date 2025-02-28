package com.sucheon.alarm.listener.context.result;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class PointNotStoreInfo implements Serializable {

    /**
     * 测点点位id
     */
    private Integer pointId;

    /**
     * 不入库字段
     */
    private List<String> notStoreFields;

}
