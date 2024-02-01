package com.hostelms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hostelms.entity.dto.Student;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper  extends BaseMapper<Student> {
}
