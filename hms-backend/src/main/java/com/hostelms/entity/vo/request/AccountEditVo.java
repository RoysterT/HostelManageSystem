package com.hostelms.entity.vo.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class AccountEditVo {
    int regOrder;
    @Length(min =  6, max = 9)
    String id;
    @Email
    String email;
    int identity;
}
