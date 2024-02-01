package com.hostelms.entity.vo.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class StudentEditVo {
    @Length(min =  9, max = 9)
    String id;
    String name;
    char gender;
    @Email
    String email;
    String phoneNum;
    String dept;
    String major;
    int classId;
    int admission;
    int graduation;
}
