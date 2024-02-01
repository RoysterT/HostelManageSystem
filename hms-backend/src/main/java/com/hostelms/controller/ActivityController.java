package com.hostelms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hostelms.entity.RestBean;
import com.hostelms.entity.dto.Activity;
import com.hostelms.entity.vo.request.ActivityEditVo;
import com.hostelms.entity.vo.request.AddActivityVo;
import com.hostelms.mapper.ActivityMapper;
import com.hostelms.service.ActivityService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hygiene")
public class ActivityController {

    @Resource
    ActivityService activityService;

    @Resource
    MessageHandle message;

    @Autowired
    ActivityMapper activityMapper;

    @GetMapping("/activity/all/list")
    public RestBean<Map<String, Object>> getActivityList(@RequestParam("page") int pageNum,
                                                         @RequestParam("pageSize") int pageSize) {
        try {
            Page<Activity> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
            activityMapper.selectPage(page, queryWrapper);
            List<Activity> records = page.getRecords();
            Map<String, Object> results = Map.of(
                    "total", page.getTotal(),
                    "currentPage", page.getCurrent(),
                    "data", records);
            return new RestBean<>(200, results, "请求成功");
        } catch (Exception e) {
            return RestBean.failure(400, "请求失败！");
        }
    }

    @GetMapping("/activity/all/array-active")
    public RestBean<Map<String, Object>[]> getActivityArray(){
        try {
            // 只获得在开始时间和结束时间之间的活动
            QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
            queryWrapper.le("start_time", new Timestamp(System.currentTimeMillis()));
            queryWrapper.ge("end_time", new Timestamp(System.currentTimeMillis()));
            List<Activity> activities = activityMapper.selectList(queryWrapper);
            Map<String, Object>[] results = new Map[activities.size()];
            for (int i = 0; i < activities.size(); i++) {
                results[i] = Map.of(
                        "value", activities.get(i).getActivityId(),
                        "label", activities.get(i).getName());
            }
            return new RestBean<>(200, results, "请求成功");
        } catch (Exception e) {
            return RestBean.failure(400, "请求失败！");
        }
    }

    @GetMapping("/activity/all/array")
    public RestBean<Map<String, Object>[]> getActivityArrayAll(){
        try {
            List<Activity> activities = activityMapper.selectList(null);
            Map<String, Object>[] results = new Map[activities.size()];
            for (int i = 0; i < activities.size(); i++) {
                results[i] = Map.of(
                        "value", activities.get(i).getActivityId(),
                        "label", activities.get(i).getName());
            }
            return new RestBean<>(200, results, "请求成功");
        } catch (Exception e) {
            return RestBean.failure(400, "请求失败！");
        }
    }

    @GetMapping("/activity/info/{activityId}")
    public RestBean<Activity> getActivity(@PathVariable("activityId") int activityId) {
        return RestBean.success(activityService.getActivityById(activityId));
    }

    @PostMapping("/activity/add")
    public RestBean<Void> addActivity(@RequestBody AddActivityVo vo) {
        return message.messageHandle(vo, activityService::addNewActivity);
    }

    @GetMapping("/activity/delete/{activityId}")
    public RestBean<Void> deleteActivity(@PathVariable("activityId") int activityId) {
        return message.messageHandle(activityId, activityService::deleteActivityById);
    }

    @PostMapping("/activity/edit")
    public RestBean<Void> editActivity(@RequestBody ActivityEditVo vo) {
        return message.messageHandle(vo, activityService::editActivityById);
    }


}
