package com.hostelms.entity.vo.request;

import lombok.Data;

@Data
public class PaymentPaidVo {
    String paymentId;
    double amount;
    String description;
    String payer;
    String paymentMethod;
}
