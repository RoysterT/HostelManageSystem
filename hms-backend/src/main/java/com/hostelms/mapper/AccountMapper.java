/**
 * AccountMapper 是用于操作 Account 实体的 MyBatis Plus Mapper 接口。
 *
 * 这个接口继承了 BaseMapper，提供了一组通用的数据库操作方法，用于操作 Account 数据库表。
 */
package com.hostelms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hostelms.entity.dto.Account;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}
