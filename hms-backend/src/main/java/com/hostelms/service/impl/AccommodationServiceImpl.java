package com.hostelms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hostelms.entity.RestBean;
import com.hostelms.entity.dto.Accommodation;
import com.hostelms.entity.vo.request.AccommodationEditVo;
import com.hostelms.mapper.AccommodationMapper;
import com.hostelms.service.AccommodationService;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AccommodationServiceImpl extends ServiceImpl<AccommodationMapper, Accommodation> implements AccommodationService {

    /**
     * 管理员删除住宿信息
     * @param id 学号 / 工号
     * @return 提示
     */
    @Override
    public String managerDeleteAccommodationById(String id) {
        boolean remove = this.removeById(id);
        if (!remove) {
            return "删除失败！";
        }
        return null;
    }

    /**
     * 管理员编辑住宿信息
     * @param vo 住宿信息
     * @return 提示
     */
    @Override
    public String managerEditAccommodationByRegOrder(AccommodationEditVo vo) {
        boolean update = this.update()
                .eq("id", vo.getId())
                .set("building", vo.getBuilding())
                .set("unit", vo.getUnit())
                .set("room", vo.getRoom())
                .set("bed", vo.getBed())
                .update();
        if (!update) {
            return "更新失败！";
        }
        return null;
    }

    /**
     * 管理员添加住宿信息
     * @param vo 住宿信息
     * @return 提示
     */
    @Override
    public String managerAddAccommodation(AccommodationEditVo vo) {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(vo.getId());
        accommodation.setBuilding(vo.getBuilding());
        accommodation.setUnit(vo.getUnit());
        accommodation.setRoom(vo.getRoom());
        if (!Objects.equals(vo.getBed(), "")) {
            accommodation.setBed(vo.getBed());
        }
        boolean save = this.save(accommodation);
        if (!save) {
            return "添加失败！";
        }
        return null;
    }

    @Override
    public String getUserAccommodation(String id) {
        Accommodation accommodation = this.query()
                .eq("id", id)
                .one();
        if (accommodation == null) {
            return RestBean.failure(400, "住宿信息不存在！").asJsonString();
        }
        return RestBean.success(accommodation).asJsonString();
    }

    @Override
    public Accommodation getUserAccommodationData(String id) {
        return this.query()
                .eq("id", id)
                .one();
    }
}
