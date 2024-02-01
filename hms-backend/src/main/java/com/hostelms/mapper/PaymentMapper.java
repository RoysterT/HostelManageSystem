package com.hostelms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hostelms.entity.dto.Payment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
