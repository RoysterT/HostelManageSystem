package com.hostelms.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Data
@TableName("Hygiene")
@AllArgsConstructor
@Getter
@Setter
public class Hygiene {
    @TableId(type = IdType.AUTO)
    int recordId;
    int activityId;
    String building;
    String unit;
    String room;
    double score;
    Timestamp recordTime;
    String description;
    int progress;
}
