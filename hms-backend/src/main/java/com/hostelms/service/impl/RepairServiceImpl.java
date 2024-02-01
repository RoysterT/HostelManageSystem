package com.hostelms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hostelms.entity.RestBean;
import com.hostelms.entity.dto.Repair;
import com.hostelms.entity.dto.Staff;
import com.hostelms.mapper.RepairMapper;
import com.hostelms.service.RepairService;
import com.hostelms.service.StaffService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class RepairServiceImpl extends ServiceImpl<RepairMapper, Repair> implements RepairService {

    @Resource
    StaffService staffService;

    @Override
    public String applyNewRepair(Repair repair) {
        repair.setCreationTime(new Timestamp(System.currentTimeMillis()));
        repair.setProgress(2);
        repair.setDormManager(staffService.getStaffByBuilding(repair.getBuilding()).getName());
        boolean save = this.save(repair);
        int repairOrder = repair.getRepairOrder();
        return save ? String.valueOf(repairOrder) : "提交失败！";
    }

    @Override
    public String getRepairByOrder(int repairOrder) {
        Repair repair = this.query()
                .eq("repair_order", repairOrder)
                .one();
        if (repair == null) {
            return RestBean.failure(400, "报修单不存在！").asJsonString();
        }
        return RestBean.success(repair).asJsonString();
    }

    @Override
    public String updateRepair(Repair repair) {
        boolean update = this.update()
                .eq("repair_order", repair.getRepairOrder())
                .set("reporter",repair.getReporter())
                .set("building", repair.getBuilding())
                .set("unit",repair.getUnit())
                .set("room", repair.getRoom())
                .set("type", repair.getType())
                .set("description", repair.getDescription())
                .set("requirement", repair.getRequirement())
                .set("email",repair.getEmail())
                .set("phone",repair.getPhone())
                .set("creation_time",repair.getCreationTime())
                .set("progress",repair.getProgress())
                .update();
        return update ? null : "更新失败！";
    }

    @Override
    public String closeRepair(int[] params) {
        boolean closed = this.update()
                .eq("repair_order", params[0])
                .set("progress", params[1])
                // 将finish_time设置为当前日期时间
                .set("finish_time", new Timestamp(System.currentTimeMillis()))
                .update();
        return closed ? null : "结束失败！";
    }

    @Override
    public String confirmRepair(int repairOrder) {
        Repair repair = this.query()
                .eq("repair_order", repairOrder)
                .one();
        if (repair == null) {
            return "报修单不存在！";
        } else if (repair.getProgress()>2) {
            return "该报修单已完成确认！";
        }
        boolean update = this.update()
                .eq("repair_order", repairOrder)
                .set("progress", 3)
                .update();
        return update ? null : "状态更新失败！";
    }

    @Override
    public String finishRepair(Object[] params) {
        Repair repair = this.query()
                .eq("repair_order", params[0])
                .one();
        if (repair == null) {
            return "报修单不存在！";
        } else if (repair.getProgress()>3) {
            return "该报修单已完成维修！";
        }
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        boolean update = this.update()
                .eq("repair_order", params[0])
                .set("worker", params[1])
                .set("progress", 4)
                .set("finish_time", currentTime)
                .update();
        return update ? null : "状态更新失败！";
    }

    @Override
    public String confirmFinish(int repairOrder) {
        Repair repair = this.query()
                .eq("repair_order", repairOrder)
                .one();
        if (repair == null) {
            return "报修单不存在！";
        } else if (repair.getProgress()>4) {
            return "该报修单已完成确认！";
        }
        boolean update = this.update()
                .eq("repair_order", repairOrder)
                .set("progress", 5)
                .update();
        return update ? null : "状态更新失败！";
    }
}
