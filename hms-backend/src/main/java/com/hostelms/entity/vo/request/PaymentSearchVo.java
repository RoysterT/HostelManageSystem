package com.hostelms.entity.vo.request;

import lombok.Data;
import lombok.Getter;

import java.sql.Timestamp;

@Data
@Getter
public class PaymentSearchVo {
    int paymentOrder;
    String paymentId;
    String building;
    String unit;
    String room;
    boolean isCreationTime;
    Timestamp[] timeRange;
    String payer;
    String paymentMethod;

    public boolean getIsCreationTime() {
        return isCreationTime;
    }
}
