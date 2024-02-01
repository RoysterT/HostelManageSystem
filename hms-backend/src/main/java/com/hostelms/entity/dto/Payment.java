package com.hostelms.entity.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.sql.Timestamp;

@Data
@TableName("Payment")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Payment {
    @TableId(type = IdType.AUTO)
    int paymentOrder;
    String paymentId;
    String building;
    String unit;
    String room;
    Timestamp creationTime;
    double amount;
    String description;
    Timestamp paymentTime;
    String payer;
    String paymentMethod;
    int progress;
}
