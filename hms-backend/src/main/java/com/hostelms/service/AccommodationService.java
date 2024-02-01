package com.hostelms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hostelms.entity.dto.Accommodation;
import com.hostelms.entity.vo.request.AccommodationEditVo;
import org.springframework.stereotype.Service;

@Service
public interface AccommodationService extends IService<Accommodation> {

    String managerDeleteAccommodationById(String id);

    String managerEditAccommodationByRegOrder(AccommodationEditVo vo);

    String managerAddAccommodation(AccommodationEditVo vo);

    String getUserAccommodation(String id);

    Accommodation getUserAccommodationData(String id);
}
