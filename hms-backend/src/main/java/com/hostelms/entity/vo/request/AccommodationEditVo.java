package com.hostelms.entity.vo.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class AccommodationEditVo {
    @Length(min =  6, max = 9)
    String id;
    String building;
    String unit;
    String room;
    String bed;
}
