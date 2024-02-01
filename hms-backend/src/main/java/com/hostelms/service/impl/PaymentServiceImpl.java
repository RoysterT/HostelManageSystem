package com.hostelms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hostelms.entity.dto.Payment;
import com.hostelms.entity.vo.request.PaymentPaidVo;
import com.hostelms.mapper.PaymentMapper;
import com.hostelms.service.PaymentService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    private String generatePaymentId(Payment payment) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String[] date = timestamp.toString().split(" ")[0].split("-");
        payment.setCreationTime(timestamp);
        String combinedInfo = payment.getBuilding()
                + payment.getUnit()
                + payment.getRoom()
                + payment.getCreationTime().hashCode()
                + payment.getAmount();
        int hashCode = combinedInfo.hashCode();
        StringBuilder hashCodeStr = new StringBuilder(String.valueOf(Math.abs(hashCode)));
        // hashCode不足10位的在前面补0
        while (hashCodeStr.length() < 10) {
            hashCodeStr.insert(0, "0");
        }
        return date[0] + date[1] + date[2] + hashCodeStr;
    }

    @Override
    public String createPayment(Payment payment) {
        String paymentId = generatePaymentId(payment);
        payment.setPaymentId(paymentId);
        payment.setProgress(2);
        boolean create = this.save(payment);
        return create ? paymentId : "订单创建失败！";
    }

    @Override
    public Payment getPaymentById(String paymentId) {
        return this.query()
                .eq("payment_id", paymentId)
                .one();
    }

    @Override
    public Payment getPaymentByOrder(int paymentOrder) {
        return this.query()
                .eq("payment_order", paymentOrder)
                .one();
    }

    @Override
    public String editPayment(Payment payment) {
        boolean update = this.update()
                .eq("payment_Order", payment.getPaymentOrder())
                .set("building", payment.getBuilding())
                .set("unit", payment.getUnit())
                .set("room", payment.getRoom())
                .set("amount", payment.getAmount())
                .set("description", payment.getDescription())
                .set("progress", payment.getProgress())
                .update();
        return update ? null : "订单更新失败！";
    }

    @Override
    public String removePaymentByOrder(String paymentOrder) {
        boolean delete = this.removeById(paymentOrder);
        return delete ? null : "订单删除失败！";
    }

    @Override
    public String confirmPayment(String paymentId) {
        boolean update = this.update()
                .eq("payment_Id", paymentId)
                .set("progress", 3)
                .update();
        return update ? null : "订单确认失败！";
    }

    @Override
    public String paidPayment(PaymentPaidVo vo) {
        boolean update = this.update()
                .eq("payment_Id", vo.getPaymentId())
                .set("progress", 4)
                .set("payment_Time", new Timestamp(System.currentTimeMillis()))
                .set("payer", vo.getPayer())
                .set("payment_Method", vo.getPaymentMethod())
                .update();
        return update ? null : "订单支付失败！";
    }
}
