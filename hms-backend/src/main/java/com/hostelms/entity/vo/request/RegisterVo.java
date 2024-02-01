package com.hostelms.entity.vo.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class RegisterVo {
    @Pattern (regexp = "^\\d{9}$|^\\d{2}-\\d{3}$")
    @Length (min = 6, max = 9)
    String id;
    @Length (min = 6, max = 50)
    String password;
    @Email
    @Length(min = 4)
    String email;
    @Length (min = 6, max = 6)
    String code;
    int identity;
}
