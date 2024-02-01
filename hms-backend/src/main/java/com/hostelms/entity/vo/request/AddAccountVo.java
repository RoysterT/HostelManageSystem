package com.hostelms.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class AddAccountVo {
    @Length(min =  6, max = 9)
    String id;
    @Email
    String email;
    @Length (min = 6, max = 50)
    String password;
    Boolean status;
    int identity;
}
