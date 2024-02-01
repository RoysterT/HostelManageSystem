package com.hostelms.entity.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@Data
@TableName ("Staff")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Staff {
    String id;
    String name;
    String gender;
    String email;
    String phoneNum;
    String manageBuilding;
    int identity;
}
