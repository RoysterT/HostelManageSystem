package com.hostelms.entity.vo.request;

import lombok.Data;
import lombok.Getter;

import java.sql.Timestamp;

@Data
public class RepairVo {
    int repairOrder;
    String reporter;
    String building;
    String unit;
    String room;
    String type;
    String description;
    String requirement;
    String email;
    String phone;
    boolean isCreationTime;
    Timestamp[] timeRange;
    String worker;
    String dormManager;
    int progress;

    public boolean getIsCreationTime() {
        return this.isCreationTime;
    }
}
