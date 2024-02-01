package com.hostelms.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class StaffEditVo {
    @Length(min =  6, max = 6)
    String id;
    String name;
    char gender;
    @Email
    String email;
    String phoneNum;
    String manageBuilding;
    int identity;
}
