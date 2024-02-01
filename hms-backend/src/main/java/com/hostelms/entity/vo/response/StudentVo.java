package com.hostelms.entity.vo.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StudentVo {
    String id;
    String name;
    String gender;
    String email;
    String phoneNum;
    String dept;
    String major;
    int classId;
    int admission;
    int graduation;
}
