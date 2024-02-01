package com.hostelms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hostelms.entity.dto.Activity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {
}
