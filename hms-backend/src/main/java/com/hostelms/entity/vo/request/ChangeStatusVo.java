package com.hostelms.entity.vo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
public class ChangeStatusVo {
    @Length(min = 6, max = 9)
    String id;
    Boolean status;
}
