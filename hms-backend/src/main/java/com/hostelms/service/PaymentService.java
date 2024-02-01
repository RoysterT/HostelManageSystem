package com.hostelms.service;

import com.hostelms.entity.dto.Payment;
import com.hostelms.entity.vo.request.PaymentPaidVo;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService {

    String createPayment(Payment payment);

    Payment getPaymentById(String paymentId);

    Payment getPaymentByOrder(int paymentOrder);

    String editPayment(Payment payment);

    String removePaymentByOrder(String paymentOrder);

    String confirmPayment(String paymentOrder);

    String paidPayment(PaymentPaidVo vo);
}
