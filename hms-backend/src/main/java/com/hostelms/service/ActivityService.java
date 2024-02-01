package com.hostelms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hostelms.entity.dto.Activity;
import com.hostelms.entity.vo.request.ActivityEditVo;
import com.hostelms.entity.vo.request.AddActivityVo;
import org.springframework.stereotype.Service;

@Service
public interface ActivityService extends IService<Activity> {
    Activity getActivityById(int activityId);

    String addNewActivity(AddActivityVo vo);

    String deleteActivityById(int activityId);

    String editActivityById(ActivityEditVo vo);
}
