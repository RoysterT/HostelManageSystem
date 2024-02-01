package com.hostelms.entity.vo.request;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ActivityEditVo {
    int activityId;
    String name;
    String description;
    Timestamp[] timeRange;
}
