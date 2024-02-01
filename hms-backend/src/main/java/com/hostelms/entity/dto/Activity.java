package com.hostelms.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.sql.Timestamp;

@Data
@TableName("Hygiene_Activity")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Activity {
    @TableId(type = IdType.AUTO)
    int activityId;
    String name;
    String description;
    Timestamp startTime;
    Timestamp endTime;
}
