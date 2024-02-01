package com.hostelms.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hostelms.entity.BaseData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@TableName("Account")
@AllArgsConstructor
@Getter
@Setter
public class Account implements BaseData {
    @TableId(type = IdType.AUTO)
    Integer regOrder;
    String id;
    String email;
    String password;
    Boolean status;
    int identity;
}
