package com.sucheon.alarm.listener.context.result;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 点位树空间
 */
@Getter
@Setter
public class PointTreeSpaceInfo implements Serializable {

    /**
     * 点位树空间
     */
    private Integer spaceId;

    /**
     * 点位树不入库信息
     */
    private List<PointNotStoreInfo> pointTree;
}
