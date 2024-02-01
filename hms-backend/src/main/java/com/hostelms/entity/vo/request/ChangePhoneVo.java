package com.hostelms.entity.vo.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class ChangePhoneVo {
    @Length (min = 6, max = 9)
    String id;
    int identity;
    @Length(min = 11, max = 11)
    String phone;
}
