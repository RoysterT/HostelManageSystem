package com.hostelms.entity.vo.request;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class HygieneSearchVo {
    int recordId;
    int activityId;
    String building;
    String unit;
    String room;
    int progress;
}
