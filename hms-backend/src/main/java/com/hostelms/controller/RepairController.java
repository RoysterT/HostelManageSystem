package com.hostelms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hostelms.entity.RestBean;
import com.hostelms.entity.dto.Repair;
import com.hostelms.entity.vo.request.RepairVo;
import com.hostelms.mapper.RepairMapper;
import com.hostelms.service.RepairService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repair")
public class RepairController {

    @Resource
    RepairService repairService;

    @Resource
    MessageHandle message;

    @Autowired
    RepairMapper repairMapper;

    @PostMapping("/apply")
    public RestBean<Map<String, Object>> addRepair(@RequestBody Repair repair) {
        String res = repairService.applyNewRepair(repair);
        if ("提交失败！".equals(res)) {
            return RestBean.failure(400, res);
        }
        Map<String, Object> result = Map.of(
                "order", res);
        return new RestBean<>(200, result, "提交成功！");
    }

    @PostMapping("/list/all")
    public RestBean<Map<String, Object>> getRepairList(@RequestParam("page") int pageNum,
                                                       @RequestParam("pageSize") int pageSize,
                                                       @RequestBody RepairVo vo) {
        try {
            Page<Repair> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Repair> queryWrapper = new QueryWrapper<>();
            if (vo.getRepairOrder() != 0) {
                queryWrapper.eq("repair_order", vo.getRepairOrder());
            }
            queryWrapper.orderByDesc("repair_order");
            queryWrapper.like("reporter", vo.getReporter());
            queryWrapper.like("building", vo.getBuilding());
            queryWrapper.like("unit", vo.getUnit());
            queryWrapper.like("room", vo.getRoom());
            queryWrapper.like("type", vo.getType());
            queryWrapper.like("description", vo.getDescription());
            queryWrapper.like("requirement", vo.getRequirement());
            queryWrapper.like("email", vo.getEmail());
            queryWrapper.like("phone", vo.getPhone());
            if (vo.getIsCreationTime()) {
                if (vo.getTimeRange() != null && vo.getTimeRange().length == 2) {
                    queryWrapper.between("creation_time", vo.getTimeRange()[0], vo.getTimeRange()[1]);
                }
            } else {
                if (vo.getTimeRange() != null && vo.getTimeRange().length == 2) {
                    queryWrapper.between("finish_time", vo.getTimeRange()[0], vo.getTimeRange()[1]);
                }
            }
            if (!"".equals(vo.getWorker())) {
                queryWrapper.like("worker", vo.getWorker());
            }
            if (!"".equals(vo.getDormManager())) {
                queryWrapper.like("dorm_manager", vo.getDormManager());
            }
            if (vo.getProgress() != 0) {
                queryWrapper.eq("progress", vo.getProgress());
            }
            repairMapper.selectPage(page, queryWrapper);
            List<Repair> records = page.getRecords();
            Map<String, Object> results = Map.of(
                    "total", page.getTotal(),
                    "currentPage", page.getCurrent(),
                    "data", records);
            return new RestBean<>(200, results, "请求成功");
        } catch (Exception e) {
            return RestBean.failure(400, "请求失败！");
        }
    }

    @GetMapping("/info/{repairOrder}")
    public String getRepair(@PathVariable("repairOrder") int repairOrder) {
        return repairService.getRepairByOrder(repairOrder);
    }

    @PostMapping("/update")
    public RestBean<Void> updateRepair(@RequestBody Repair repair) {
        return message.messageHandle(repair, repairService::updateRepair);
    }

    @GetMapping("/close")
    public RestBean<Void> closeRepair(@RequestParam("repairOrder") int repairOrder,
                                      @RequestParam("setType") int setType) {
        return message.messageHandle(new int[]{repairOrder, setType}, repairService::closeRepair);
    }

    @GetMapping("/list/to-confirm")
    public RestBean<Map<String, Object>> getRepairList(@RequestParam("page") int pageNum,
                                                       @RequestParam("pageSize") int pageSize,
                                                       @RequestParam("building") String building,
                                                       @RequestParam("type") String type) {
        try {
            Page<Repair> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Repair> queryWrapper = new QueryWrapper<>();
            if ("confirm".equals(type)) {
                queryWrapper.eq("progress", 2);
            }
            queryWrapper.like("building", building);
            repairMapper.selectPage(page, queryWrapper);
            List<Repair> records = page.getRecords();
            Map<String, Object> results = Map.of(
                    "total", page.getTotal(),
                    "currentPage", page.getCurrent(),
                    "data", records);
            return new RestBean<>(200, results, "请求成功");
        } catch (Exception e) {
            return RestBean.failure(400, "请求失败！");
        }
    }

    @GetMapping("/confirm")
    public RestBean<Void> confirmRepair(@RequestParam("repairOrder") int repairOrder) {
        return message.messageHandle(repairOrder, repairService::confirmRepair);
    }

    @GetMapping("/history")
    public RestBean<Map<String, Object>> getHistory(@RequestParam("page") int pageNum,
                                                    @RequestParam("pageSize") int pageSize,
                                                    @RequestParam("building") String building,
                                                    @RequestParam("unit") String unit,
                                                    @RequestParam("room") String room) {
        try {
            Page<Repair> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Repair> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("repair_order");
            queryWrapper.like("building", building);
            queryWrapper.like("unit", unit);
            queryWrapper.like("room", room);
            repairMapper.selectPage(page, queryWrapper);
            List<Repair> records = page.getRecords();
            Map<String, Object> results = Map.of(
                    "total", page.getTotal(),
                    "currentPage", page.getCurrent(),
                    "data", records);
            return new RestBean<>(200, results, "请求成功");
        } catch (Exception e) {
            return RestBean.failure(400, "请求失败！");
        }
    }

    @GetMapping("/finish-repair")
    public RestBean<Void> finishRepair(@RequestParam("repairOrder") int repairOrder,
                                       @RequestParam("worker") String workerName) {
        return message.messageHandle(new Object[]{repairOrder, workerName}, repairService::finishRepair);
    }

    @GetMapping("confirm-finish")
    public RestBean<Void> confirmFinish(@RequestParam("repairOrder") int repairOrder) {
        return message.messageHandle(repairOrder, repairService::confirmFinish);
    }
}
