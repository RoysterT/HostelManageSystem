package com.hostelms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hostelms.entity.dto.Identity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IdentityMapper extends BaseMapper<Identity> {
}
