package com.hostelms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hostelms.entity.RestBean;
import com.hostelms.entity.dto.Accommodation;
import com.hostelms.entity.dto.Hygiene;
import com.hostelms.entity.dto.Student;
import com.hostelms.entity.vo.request.HygieneSearchVo;
import com.hostelms.mapper.HygieneMapper;
import com.hostelms.service.*;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hygiene")
public class HygieneController {

    @Resource
    HygieneService hygieneService;

    @Resource
    AccommodationService accommodationService;

    @Resource
    MessageHandle message;

    @Autowired
    HygieneMapper hygieneMapper;

    @PostMapping("/mark/list")
    public RestBean<Map<String, Object>> getMarkList(@RequestParam("page") int pageNum,
                                                     @RequestParam("pageSize") int pageSize,
                                                     @RequestParam("type") String type,
                                                     @RequestBody HygieneSearchVo vo) {
        try {
            Page<Hygiene> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Hygiene> queryWrapper = new QueryWrapper<>();
            if (vo.getRecordId() != 0) {
                queryWrapper.eq("record_id", vo.getRecordId());
            }
            if (vo.getActivityId() != 0) {
                queryWrapper.eq("activity_id", vo.getActivityId());
            }
            queryWrapper.like("building", vo.getBuilding());
            queryWrapper.like("unit", vo.getUnit());
            queryWrapper.like("room", vo.getRoom());
            if ("confirm".equals(type)) {
                queryWrapper.eq("progress", 3);
            }
            hygieneMapper.selectPage(page, queryWrapper);
            List<Hygiene> records = page.getRecords();
            Map<String, Object> results = Map.of(
                    "total", page.getTotal(),
                    "currentPage", page.getCurrent(),
                    "data", records);
            return new RestBean<>(200, results, "请求成功");
        } catch (Exception e) {
            return RestBean.failure(400, "请求失败！");
        }
    }

    @PostMapping("/mark/add")
    public RestBean<Void> addMark(@RequestBody Hygiene hygiene) {
        return message.messageHandle(hygiene, hygieneService::addNewMark);
    }

    @GetMapping("/mark/info/{recordId}")
    public RestBean<Hygiene> getHygieneMarkInfo(@PathVariable("recordId") int recordId) {
        Hygiene hygiene = hygieneService.getHygieneMarkInfo(recordId);
        if (hygiene == null) {
            return RestBean.failure(400, "请求失败！");
        }
        return new RestBean<>(200, hygiene, "请求成功");
    }

    @PostMapping("/mark/edit")
    public RestBean<Void> editMark(@RequestBody Hygiene hygiene) {
        return message.messageHandle(hygiene, hygieneService::updateHygieneMarkInfo);
    }

    @GetMapping("/mark/delete/{recordId}")
    public RestBean<Void> deleteMark(@PathVariable("recordId") int recordId) {
        String result = hygieneService.deleteHygieneMarkInfo(recordId);
        if (result != null) {
            return RestBean.failure(400, result);
        }
        return new RestBean<>(200, null, "请求成功");
    }

    @GetMapping("/mark/rectify/finish/{recordId}")
    public RestBean<Void> rectifyFinish(@PathVariable("recordId") int recordId) {
        return message.messageHandle(recordId, hygieneService::rectifyFinish);
    }

    @GetMapping("/mark/rectify/success/{recordId}")
    public RestBean<Void> rectitySuccess(@PathVariable("recordId") int recordId) {
        return message.messageHandle(recordId, hygieneService::rectitySuccess);
    }

    @GetMapping("/score/info/latest/{studentId}")
    public RestBean<Hygiene> getLatestScoreInfo(@PathVariable("studentId") int studentId) {
        Accommodation accommodation = accommodationService.query()
                .eq("id", studentId)
                .one();
        Hygiene hygiene = hygieneService.query()
                .eq("building", accommodation.getBuilding())
                .eq("unit", accommodation.getUnit())
                .eq("room", accommodation.getRoom())
                .orderByDesc("record_id")
                .last("limit 1")
                .one();
        if (hygiene == null) {
            return RestBean.failure(400, "请求失败！");
        }
        return new RestBean<>(200, hygiene, "请求成功");
    }

    @GetMapping("/score/info/list/{studentId}")
    public RestBean<Map<String, Object>> getDormHygieneList(@PathVariable("studentId") String studentId,
                                                            @RequestParam("page") int pageNum,
                                                            @RequestParam("pageSize") int pageSize) {
        Accommodation accommodation = accommodationService.getUserAccommodationData(studentId);
        Page<Hygiene> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Hygiene> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("building", accommodation.getBuilding());
        queryWrapper.eq("unit", accommodation.getUnit());
        queryWrapper.eq("room", accommodation.getRoom());
        queryWrapper.orderByDesc("record_id");
        hygieneMapper.selectPage(page, queryWrapper);
        List<Hygiene> records = page.getRecords();
        Map<String, Object> results = Map.of(
                "total", page.getTotal(),
                "currentPage", page.getCurrent(),
                "data", records);
        return new RestBean<>(200, results, "请求成功");
    }
}
