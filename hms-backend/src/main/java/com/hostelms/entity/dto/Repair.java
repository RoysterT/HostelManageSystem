package com.hostelms.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.sql.Timestamp;

@Data
@TableName("Repair")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Repair {
    @TableId(type = IdType.AUTO)
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
    Timestamp creationTime;
    Timestamp finishTime;
    String worker;
    String dormManager;
    int progress;
}
