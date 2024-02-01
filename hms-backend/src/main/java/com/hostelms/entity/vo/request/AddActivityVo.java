package com.hostelms.entity.vo.request;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class AddActivityVo {
    String name;
    String description;
    Timestamp[] timeRange;
}
