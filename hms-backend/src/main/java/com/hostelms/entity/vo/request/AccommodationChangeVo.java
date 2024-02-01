package com.hostelms.entity.vo.request;

import lombok.Data;

import java.sql.Time;
import java.sql.Timestamp;

@Data
public class AccommodationChangeVo {
    String studentIdA;
    String studentIdB;
    String description;
}
