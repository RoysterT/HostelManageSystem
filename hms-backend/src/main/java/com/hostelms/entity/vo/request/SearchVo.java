package com.hostelms.entity.vo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchVo<T> {
    // 分页信息
//    private Integer page = 1;
//    private Integer pageSize = 10;
    // 搜索信息
    private T data;
}
