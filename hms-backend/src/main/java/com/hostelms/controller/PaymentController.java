package com.hostelms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hostelms.entity.RestBean;
import com.hostelms.entity.dto.Accommodation;
import com.hostelms.entity.dto.Payment;
import com.hostelms.entity.dto.Repair;
import com.hostelms.entity.vo.request.PaymentPaidVo;
import com.hostelms.entity.vo.request.PaymentSearchVo;
import com.hostelms.mapper.PaymentMapper;
import com.hostelms.service.AccommodationService;
import com.hostelms.service.PaymentService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Resource
    PaymentService paymentService;

    @Resource
    AccommodationService accommodationService;

    @Resource
    MessageHandle message;

    @Autowired
    PaymentMapper paymentMapper;

    @PostMapping("/create")
    public RestBean<Map<String, Object>> addPayment(@RequestBody Payment payment) {
        String res = paymentService.createPayment(payment);
        if ("订单创建失败！".equals(res)) {
            return RestBean.failure(400, res);
        }
        int paymentOrder = paymentService.getPaymentById(res).getPaymentOrder();
        Map<String, Object> result = Map.of(
                "paymentOrder", paymentOrder,
                "paymentId", res);
        return new RestBean<>(200, result, "提交成功！");
    }

    @GetMapping("/delete")
    public RestBean<Void> deletePayment(@RequestParam("paymentOrder") String paymentOrder) {
        return message.messageHandle(paymentOrder, paymentService::removePaymentByOrder);
    }

    @GetMapping("/info/id/{paymentId}")
    public String getPaymentById(@PathVariable("paymentId") String paymentId) {
        Payment payment = paymentService.getPaymentById(paymentId);
        if (payment == null) {
            return RestBean.failure(400, "订单不存在！").asJsonString();
        }
        return RestBean.success(payment).asJsonString();
    }

    @GetMapping("/info/order/{paymentOrder}")
    public String getPaymentByOrder(@PathVariable("paymentOrder") int paymentOrder) {
        Payment payment = paymentService.getPaymentByOrder(paymentOrder);
        if (payment == null) {
            return RestBean.failure(400, "订单不存在！").asJsonString();
        }
        return RestBean.success(payment).asJsonString();
    }

    @PostMapping("/list/all")
    public RestBean<Map<String, Object>> getPaymentList(@RequestParam("page") int pageNum,
                                                        @RequestParam("pageSize") int pageSize,
                                                        @RequestBody PaymentSearchVo vo) {
        try {
            Page<Payment> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Payment> queryWrapper = new QueryWrapper<>();
            if (vo.getPaymentOrder() != 0) {
                queryWrapper.eq("payment_order", vo.getPaymentOrder());
            }
            queryWrapper.orderByDesc("payment_order");
            queryWrapper.like("payment_id", vo.getPaymentId());
            queryWrapper.like("building", vo.getBuilding());
            queryWrapper.like("unit", vo.getUnit());
            queryWrapper.like("room", vo.getRoom());
            if (vo.getIsCreationTime()) {
                if (vo.getTimeRange() != null && vo.getTimeRange().length == 2) {
                    queryWrapper.between("creation_time", vo.getTimeRange()[0], vo.getTimeRange()[1]);
                }
            } else {
                if (vo.getTimeRange() != null && vo.getTimeRange().length == 2) {
                    queryWrapper.between("payment_time", vo.getTimeRange()[0], vo.getTimeRange()[1]);
                }
            }
            if (!"".equals(vo.getPayer())) {
                queryWrapper.like("payer", vo.getPayer());
            }
            if (!"".equals(vo.getPaymentMethod())) {
                queryWrapper.like("payment_method", vo.getPaymentMethod());
            }
            paymentMapper.selectPage(page, queryWrapper);
            Map<String, Object> results = Map.of(
                    "total", page.getTotal(),
                    "currentPage", page.getCurrent(),
                    "data", page.getRecords());
            return new RestBean<>(200, results, "请求成功");
        } catch (Exception e) {
            return RestBean.failure(400, "请求失败！");
        }
    }

    @GetMapping("/list/building/{building}/{type}")
    public RestBean<Map<String, Object>> getPaymentListByBuilding(@RequestParam("page") int pageNum,
                                                                  @RequestParam("pageSize") int pageSize,
                                                                  @PathVariable("building") String building,
                                                                  @PathVariable("type") String type) {
        try {
            Page<Payment> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Payment> queryWrapper = new QueryWrapper<>();
            if ("confirm".equals(type)) {
                queryWrapper.eq("progress", 2);
            }
            queryWrapper.like("building", building);
            paymentMapper.selectPage(page, queryWrapper);
            List<Payment> records = page.getRecords();
            Map<String, Object> results = Map.of(
                    "total", page.getTotal(),
                    "currentPage", page.getCurrent(),
                    "data", records);
            return new RestBean<>(200, results, "请求成功");
        } catch (Exception e) {
            return RestBean.failure(400, "请求失败！");
        }
    }

    @GetMapping("/confirm")
    public RestBean<Void> confirmPayment(@RequestParam("paymentId") String paymentId) {
        return message.messageHandle(paymentId, paymentService::confirmPayment);
    }

    @GetMapping("/list/room/{userId}/{type}")
    public RestBean<Map<String, Object>> getPaymentListByUser(@RequestParam("page") int pageNum,
                                                              @RequestParam("pageSize") int pageSize,
                                                              @PathVariable("userId") String userId,
                                                              @PathVariable("type") String type) {
        try {
            Page<Payment> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Payment> queryWrapper = new QueryWrapper<>();
            // 获得用户住宿信息
            Accommodation accommodation = accommodationService.getUserAccommodationData(userId);
            if (accommodation == null) {
                return RestBean.failure(400, "未查询到用户住宿信息！");
            }
            if ("paid".equals(type)) {
                queryWrapper.eq("progress", 3);
            }
            queryWrapper.eq("building", accommodation.getBuilding());
            queryWrapper.eq("unit", accommodation.getUnit());
            queryWrapper.eq("room", accommodation.getRoom());
            paymentMapper.selectPage(page, queryWrapper);
            Map<String, Object> results = Map.of(
                    "total", page.getTotal(),
                    "currentPage", page.getCurrent(),
                    "data", page.getRecords());
            return new RestBean<>(200, results, "请求成功");
        } catch (Exception e) {
            return RestBean.failure(400, "请求失败！");
        }
    }

    @PostMapping("/edit")
    public RestBean<Void> editPayment(@RequestBody Payment payment) {
        return message.messageHandle(payment, paymentService::editPayment);
    }

    @PostMapping("/paid")
    public RestBean<Void> paidPayment(@RequestBody PaymentPaidVo vo) {
        return message.messageHandle(vo, paymentService::paidPayment);
    }
}
