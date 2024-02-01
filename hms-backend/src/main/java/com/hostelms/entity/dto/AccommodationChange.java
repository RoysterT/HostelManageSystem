package com.hostelms.entity.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.sql.Timestamp;

@Data
@TableName("change_accommodation")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccommodationChange {
    int recordOrder;
    String studentIdA;
    String studentBuildingA;
    String studentIdB;
    String studentBuildingB;
    String description;
    Timestamp creationTime;
    Timestamp finishTime;
    int progress;
}
