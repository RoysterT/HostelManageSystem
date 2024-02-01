package com.hostelms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hostelms.entity.dto.Hygiene;
import com.hostelms.mapper.HygieneMapper;
import com.hostelms.service.HygieneService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class HygieneServiceImpl extends ServiceImpl<HygieneMapper, Hygiene> implements HygieneService {
    @Override
    public String addNewMark(Hygiene hygiene) {
        hygiene.setProgress(2);
        hygiene.setRecordTime(new Timestamp(System.currentTimeMillis()));
        hygiene.setProgress(hygiene.getScore() < 60 ? 2 : 4);
        boolean save = this.save(hygiene);
        return save ? null : "新增失败";
    }

    @Override
    public Hygiene getHygieneMarkInfo(int recordId) {
        return this.query()
                .eq("record_id", recordId)
                .one();
    }

    @Override
    public String updateHygieneMarkInfo(Hygiene hygiene) {
        boolean update = this.update()
                .eq("record_id", hygiene.getRecordId())
                .set("activity_id", hygiene.getActivityId())
                .set("building", hygiene.getBuilding())
                .set("unit", hygiene.getUnit())
                .set("room", hygiene.getRoom())
                .set("score", hygiene.getScore())
                .set("record_time", new Timestamp(System.currentTimeMillis()))
                .set("description", hygiene.getDescription())
                .set("progress", hygiene.getScore() < 60 ? 2 : 4)
                .update();
        return update ? null : "修改失败";
    }

    @Override
    public String deleteHygieneMarkInfo(int recordId) {
        boolean remove = this.removeById(recordId);
        return remove ? null : "删除失败";
    }

    @Override
    public String rectifyFinish(int recordId) {
        boolean update = this.update()
                .eq("record_id", recordId)
                .set("progress", 3)
                .update();
        return update ? null : "修改失败";
    }

    @Override
    public String rectitySuccess(int recordId) {
        boolean update = this.update()
                .eq("record_id", recordId)
                .set("progress", 4)
                .update();
        return update ? null : "修改失败";
    }
}
