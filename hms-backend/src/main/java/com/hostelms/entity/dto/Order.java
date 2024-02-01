package com.hostelms.entity.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Data
@TableName("water_order")
@AllArgsConstructor
@Getter
@Setter
public class Order {
    int orderNum;
    String orderId;
    String building;
    String unit;
    String room;
    Timestamp orderTime;
    double univalent;
    int quantity;
    String buyer;
    String delivery;
    Timestamp FinishTime;
    int progress;
}
