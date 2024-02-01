package com.hostelms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hostelms.entity.dto.AccommodationChange;
import com.hostelms.entity.dto.Hygiene;
import com.hostelms.entity.vo.request.AccommodationChangeVo;
import com.hostelms.entity.vo.response.ChangeAccRespVo;
import org.springframework.stereotype.Service;

@Service
public interface DormService extends IService<AccommodationChange> {

    String applyChangeDorm(AccommodationChangeVo vo);

    ChangeAccRespVo getLatestApply(String userId);

    AccommodationChange getAccInfo(int recordOrder);

    String updateProgress(int recordOrder, Integer progress);

    String updateAccommodationChange(int recordOrder);
}
