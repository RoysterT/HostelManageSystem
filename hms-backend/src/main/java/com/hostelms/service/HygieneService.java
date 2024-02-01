package com.hostelms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hostelms.entity.dto.Hygiene;
import org.springframework.stereotype.Service;

@Service
public interface HygieneService extends IService<Hygiene> {

    String addNewMark(Hygiene hygiene);

    Hygiene getHygieneMarkInfo(int recordId);

    String updateHygieneMarkInfo(Hygiene hygiene);

    String deleteHygieneMarkInfo(int recordId);

    String rectifyFinish(int recordId);

    String rectitySuccess(int recordId);
}
