package com.sucheon.alarm.event.pointtree;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class PointTreeSpace implements Serializable {

    /**
     * 点位树空间id
     */
    private String spaceId;

    private List<PointOriginData> pointIds;
}
