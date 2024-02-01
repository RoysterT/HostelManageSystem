package com.hostelms.entity.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 住宿信息
 */
@Data
@TableName("Accommodation")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Accommodation {
    String id;
    String building;
    String unit;
    String room;
    String bed;
}
