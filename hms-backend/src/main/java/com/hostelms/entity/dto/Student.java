package com.hostelms.entity.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@Data
@TableName ("Student")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Student {
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
