package com.hostelms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hostelms.entity.RestBean;
import com.hostelms.entity.dto.*;
import com.hostelms.entity.vo.request.AccommodationChangeVo;
import com.hostelms.entity.vo.response.ChangeAccRespVo;
import com.hostelms.mapper.AccommodationChangeMapper;
import com.hostelms.service.AccommodationService;
import com.hostelms.service.DormService;
import com.hostelms.service.StaffService;
import com.hostelms.service.StudentService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dorm")
public class DormController {

    @Resource
    DormService dormService;

    @Resource
    AccommodationService accommodationService;

    @Resource
    StudentService studentService;

    @Resource
    StaffService staffService;

    @Resource
    MessageHandle message;

    @Autowired
    AccommodationChangeMapper accommodationChangeMapper;

    @GetMapping("/list/{type}")
    public RestBean<Map<String, Object>> getAllList(@PathVariable("type") String type,
                                                    @RequestParam("page") Integer pageNum,
                                                    @RequestParam("pageSize") Integer pageSize) {
        try {
            Page<AccommodationChange> page = new Page<>(pageNum, pageSize);
            QueryWrapper<AccommodationChange> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("record_order");
            if ("confirm".equals(type)) {
                queryWrapper.eq("progress", 4);
            }
            accommodationChangeMapper.selectPage(page, queryWrapper);
            List<AccommodationChange> records = page.getRecords();
            Map<String, Object> results = Map.of(
                    "total", page.getTotal(),
                    "currentPage", page.getCurrent(),
                    "data", records);
            return new RestBean<>(200, results, "请求成功");
        } catch (Exception e) {
            return RestBean.failure(400, "请求失败！");
        }
    }

    @PostMapping("/apply/new")
    public RestBean<Void> applyChangeDorm(@RequestBody AccommodationChangeVo vo) {
        try {
            String res = dormService.applyChangeDorm(vo);
            if (res != null) {
                return RestBean.failure(400, res);
            }
            return RestBean.success(null);
        } catch (Exception e) {
            return RestBean.failure(400, "提交失败！");
        }
    }

    @GetMapping("/apply/info/id/{userId}")
    public String getLatestApply(@PathVariable("userId") String userId) {
        return RestBean.success(dormService.getLatestApply(userId)).asJsonString();
    }

    @GetMapping("/apply/info/order/{recordOrder}")
    public RestBean<ChangeAccRespVo> getAccommodationChangeInfo(@PathVariable("recordOrder") int recordOrder) {
        ChangeAccRespVo vo = new ChangeAccRespVo();
        vo.setRecordOrder(recordOrder);
        AccommodationChange ac = dormService.getAccInfo(recordOrder);
        if (ac == null) {
            return RestBean.failure(400, "未找到记录！");
        }
        Accommodation accommodationA = accommodationService.getUserAccommodationData(ac.getStudentIdA());
        Accommodation accommodationB = accommodationService.getUserAccommodationData(ac.getStudentIdB());
        Student studentA = studentService.getStudentById(ac.getStudentIdA());
        Student studentB = studentService.getStudentById(ac.getStudentIdB());
        Staff staffA = staffService.getStaffByBuilding(accommodationA.getBuilding());
        Staff staffB = staffService.getStaffByBuilding(accommodationB.getBuilding());

        vo.setStudentIdA(ac.getStudentIdA());
        vo.setStudentNameA(studentA.getName());
        vo.setBuildingA(accommodationA.getBuilding());
        vo.setUnitA(accommodationA.getUnit());
        vo.setRoomA(accommodationA.getRoom());
        vo.setBuildingManagerA(staffA.getName());

        vo.setStudentIdB(ac.getStudentIdB());
        vo.setStudentNameB(studentB.getName());
        vo.setBuildingB(accommodationB.getBuilding());
        vo.setUnitB(accommodationB.getUnit());
        vo.setRoomB(accommodationB.getRoom());
        vo.setBuildingManagerB(staffB.getName());

        vo.setDescription(ac.getDescription());
        vo.setCreationTime(new Date(ac.getCreationTime().getTime()).toString());
        vo.setFinishTime(ac.getFinishTime() != null ? new Date(ac.getFinishTime().getTime()).toString() : "未完成");
        vo.setProgress(ac.getProgress());

        return RestBean.success(vo);
    }

    @GetMapping("/building/list/{building}")
    public RestBean<Map<String, Object>> getBuildingList(@PathVariable("building") int building,
                                                         @RequestParam("page") Integer pageNum,
                                                         @RequestParam("pageSize") Integer pageSize,
                                                         @RequestParam("type") String type) {
        try {
            Page<AccommodationChange> page = new Page<>(pageNum, pageSize);
            QueryWrapper<AccommodationChange> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("record_order");
            if ("confirm".equals(type)) {
                queryWrapper.eq("progress", 2).eq("student_building_a", building)
                        .or().eq("progress", 3).eq("student_building_b", building);
            }
            else {
                queryWrapper.eq("student_building_a", building)
                        .or()
                        .eq("student_building_b", building);
            }
            accommodationChangeMapper.selectPage(page, queryWrapper);
            List<AccommodationChange> records = page.getRecords();
            Map<String, Object> results = Map.of(
                    "total", page.getTotal(),
                    "currentPage", page.getCurrent(),
                    "data", records);
            return new RestBean<>(200, results, "请求成功");
        } catch (Exception e) {
            return RestBean.failure(400, "请求失败！");
        }
    }

    @GetMapping("/building/confirm/{recordOrder}")
    public RestBean<Void> confirmChange(@PathVariable("recordOrder") int recordOrder) {
        AccommodationChange ac = dormService.getAccInfo(recordOrder);
        String studentA = ac.getStudentIdA();
        String studentB = ac.getStudentIdB();
        String buildingA = accommodationService.getUserAccommodationData(studentA).getBuilding();
        String buildingB = accommodationService.getUserAccommodationData(studentB).getBuilding();
        // 第一个宿管员确认
        if (ac.getProgress() == 2) {
            // 交换二人在同一栋楼
            if (buildingA.equals(buildingB)) {
                dormService.updateProgress(recordOrder, 4);
            }
            // 交换二人不在同一栋楼
            else {
                dormService.updateProgress(recordOrder, 3);
            }
        }
        // 第二个宿管员确认
        else if (ac.getProgress() == 3) {
            dormService.updateProgress(recordOrder, 4);
        }
        return RestBean.success(null);
    }

    @GetMapping("/manage/confirm/{recordOrder}")
    public RestBean<Void> managerConfirmChange(@PathVariable("recordOrder") int recordOrder){
        return message.messageHandle(recordOrder, dormService::updateAccommodationChange);
    }

    @GetMapping("/building/reject/{recordOrder}")
    public RestBean<Void> rejectChange(@PathVariable("recordOrder") int recordOrder,
                                       @RequestParam("progress") int progress) {
        dormService.updateProgress(recordOrder, progress);
        return RestBean.success(null);
    }

    @GetMapping("/manage/reject/{recordOrder}")
    public RestBean<String> managerRejectChange(@PathVariable("recordOrder") int recordOrder,
                                                @RequestParam("progress") int progress) {
        return RestBean.success(dormService.updateProgress(recordOrder, progress));
    }
}
