package com.hostelms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hostelms.entity.dto.Activity;
import com.hostelms.entity.dto.Hygiene;
import com.hostelms.entity.vo.request.ActivityEditVo;
import com.hostelms.entity.vo.request.AddActivityVo;
import com.hostelms.mapper.ActivityMapper;
import com.hostelms.mapper.HygieneMapper;
import com.hostelms.service.ActivityService;
import com.hostelms.service.HygieneService;
import org.springframework.stereotype.Service;

@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {
    @Override
    public Activity getActivityById(int activityId) {
        return this.query()
                .eq("activity_id", activityId)
                .one();
    }

    @Override
    public String addNewActivity(AddActivityVo vo) {
        Activity activity = new Activity();
        activity.setName(vo.getName());
        activity.setDescription(vo.getDescription());
        activity.setStartTime(vo.getTimeRange()[0]);
        activity.setEndTime(vo.getTimeRange()[1]);
        boolean save = this.save(activity);
        return save ? null : "新增失败";
    }

    @Override
    public String deleteActivityById(int activityId) {
        boolean remove = this.removeById(activityId);
        return remove ? null : "删除失败";
    }

    @Override
    public String editActivityById(ActivityEditVo vo) {
        boolean update = this.update()
                .eq("activity_id", vo.getActivityId())
                .set("name", vo.getName())
                .set("description", vo.getDescription())
                .set("start_time", vo.getTimeRange()[0])
                .set("end_time", vo.getTimeRange()[1])
                .update();
        return update ? null : "修改失败";
    }
}
