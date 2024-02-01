package com.hostelms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hostelms.entity.dto.Accommodation;
import com.hostelms.entity.dto.AccommodationChange;
import com.hostelms.entity.vo.request.AccommodationChangeVo;
import com.hostelms.entity.vo.response.ChangeAccRespVo;
import com.hostelms.mapper.AccommodationChangeMapper;
import com.hostelms.service.AccommodationService;
import com.hostelms.service.DormService;
import com.hostelms.service.StaffService;
import com.hostelms.service.StudentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

@Service
public class DormServiceImpl extends ServiceImpl<AccommodationChangeMapper, AccommodationChange> implements DormService {

    @Resource
    AccommodationService accommodationService;

    @Resource
    StudentService studentService;

    @Resource
    StaffService staffService;


    @Override
    public String applyChangeDorm(AccommodationChangeVo vo) {
        AccommodationChange ac = new AccommodationChange();
        Accommodation accommodationA = accommodationService.getUserAccommodationData(vo.getStudentIdA());
        Accommodation accommodationB = accommodationService.getUserAccommodationData(vo.getStudentIdB());
        ac.setStudentIdA(vo.getStudentIdA());
        ac.setStudentBuildingA(accommodationA.getBuilding());
        ac.setStudentIdB(vo.getStudentIdB());
        ac.setStudentBuildingB(accommodationB.getBuilding());
        ac.setDescription(vo.getDescription());
        ac.setProgress(2);
        ac.setCreationTime(new Timestamp(System.currentTimeMillis()));
        boolean apply = this.save(ac);
        return apply ? null : "提交失败！";
    }

    @Override
    public ChangeAccRespVo getLatestApply(String userId) {
        // 最近一条progress小于4的记录，即未处理的记录，可以无结果
        AccommodationChange accommodationChange = this.query()
                .eq("student_id_a", userId)
                .or()
                .eq("student_id_b", userId)
//                .lt("progress", 5)
                .orderByDesc("creation_time")
                .last("limit 1")
                .one();
        if (accommodationChange == null) {
            return null;
        }
        ChangeAccRespVo respVo = new ChangeAccRespVo();
        respVo.setRecordOrder(accommodationChange.getRecordOrder());
        respVo.setStudentIdA(accommodationChange.getStudentIdA());
        respVo.setStudentIdB(accommodationChange.getStudentIdB());
        respVo.setDescription(accommodationChange.getDescription());
        respVo.setCreationTime(accommodationChange.getCreationTime().toString());
        respVo.setProgress(accommodationChange.getProgress());
        respVo.setFinishTime(accommodationChange.getFinishTime() == null ? "未完成" : accommodationChange.getFinishTime().toString());
        return respVo;
    }

    @Override
    public AccommodationChange getAccInfo(int recordOrder) {
        return this.query()
                .eq("record_order", recordOrder)
                .one();
    }

    @Override
    public String updateProgress(int recordOrder, Integer progress) {
        boolean update = this.update()
                .eq("record_order", recordOrder)
                .set("progress", progress)
                .update();
        return update ? null : "更新失败！";
    }

    @Override
    public String updateAccommodationChange(int recordOrder) {
        AccommodationChange ac = this.query()
                .eq("record_order", recordOrder)
                .one();
        Accommodation accommodationA = accommodationService.getUserAccommodationData(ac.getStudentIdA());
        Accommodation accommodationB = accommodationService.getUserAccommodationData(ac.getStudentIdB());
        boolean updateA = accommodationService.update()
                .eq("id", ac.getStudentIdA())
                .set("building", accommodationB.getBuilding())
                .set("unit", accommodationB.getUnit())
                .set("room", accommodationB.getRoom())
                .update();
        boolean updateB = accommodationService.update()
                .eq("id", ac.getStudentIdB())
                .set("building", accommodationA.getBuilding())
                .set("unit", accommodationA.getUnit())
                .set("room", accommodationA.getRoom())
                .update();
        boolean update = this.update()
                .eq("record_order", recordOrder)
                .set("progress", 5)
                .set("finish_time", new Timestamp(System.currentTimeMillis()))
                .update();
        return update && updateA && updateB ? null : "更新失败！";
    }
}
