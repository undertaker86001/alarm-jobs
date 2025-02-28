package com.sucheon.alarm.event.pointtree;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class PointOriginData implements Serializable {

    /**
     * 测点id
     */
    private String pointId;

    /**
     * 不入库字段
     */
    private List<String> noDbFields;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointOriginData that = (PointOriginData) o;
        return Objects.equals(pointId, that.pointId);
    }
}
