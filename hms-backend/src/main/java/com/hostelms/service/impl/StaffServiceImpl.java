package com.hostelms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hostelms.entity.RestBean;
import com.hostelms.entity.dto.Staff;
import com.hostelms.entity.vo.request.ChangePhoneVo;
import com.hostelms.entity.vo.request.StaffEditVo;
import com.hostelms.entity.vo.response.StaffVo;
import com.hostelms.mapper.StaffMapper;
import com.hostelms.service.StaffService;
import com.hostelms.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class StaffServiceImpl extends ServiceImpl<StaffMapper, Staff> implements StaffService {

    @Resource
    FlowUtils utils;

    @Resource
    StaffMapper staffMapper;

    @Override
    public int getStaffIdentity(String id) {
        Staff data = this.query()
                .eq("id", id)
                .one();
        return data.getIdentity();
    }

    /**
     * 根据用户名查找用户账户。
     *
     * @param id 工号
     * @return Account 用户账户对象
     */
    @Override
    public String findStaffById(String id) {
        StaffVo vo = new StaffVo();
        Staff data = this.query()
                .eq("id", id)
                .one();
        vo.setId(id);
        vo.setName(data.getName());
        vo.setGender(data.getGender());
        vo.setEmail(data.getEmail());
        vo.setPhoneNum(data.getPhoneNum());
        vo.setManageBuilding(data.getManageBuilding());
        return RestBean.success(vo).asJsonString();
    }

    /**
     * 根据学生 ID 获取姓名。
     *
     * @param id 工号
     * @return String 姓名，如果找到则返回姓名，否则返回 null
     */
    @Override
    public String getTrueNameById(String id) {
        // 先从 student 表中查询学生姓名
        QueryWrapper<Staff> staffQuery = new QueryWrapper<>();
        staffQuery.eq("id", id);
        Staff staff = staffMapper.selectOne(staffQuery);

        if (staff != null) {
            return staff.getName();
        }
        return null;
    }

    /**
     * 更新职员数据表手机号
     * @param vo 职员信息
     * @return 提示
     */
    @Override
    public String changePhone(ChangePhoneVo vo) {
        boolean update = this.update()
                .eq("id",vo.getId())
                .set("phone_Num",vo.getPhone())
                .update();
        if (!update){
            return "更新失败！";
        }
        return null;
    }

    /**
     * 更新职员数据表邮箱
     * @param id 职员工号
     * @param email 新邮箱
     * @return 提示
     */
    @Override
    public String updateEmail(String id, String email) {
        boolean update = this.update()
                .eq("id",id)
                .set("email",email)
                .update();
        if (!update){
            return "更新失败！";
        }
        return null;
    }

    /**
     * 管理员根据工号删除职员
     * @param id 职员工号
     * @return 提示
     */
    @Override
    public String managerDeleteStaffById(String id) {
        boolean remove = this.removeById(id);
        if (!remove) {
            return "删除失败！";
        }
        return null;
    }

    /**
     * 管理员根据工号修改职员信息
     * @param vo 职员信息
     * @return 提示
     */
    @Override
    public String managerEditStaffById(StaffEditVo vo) {
        boolean update = this.update()
                .eq("id", vo.getId())
                .set("name", vo.getName())
                .set("gender", vo.getGender())
                .set("email", vo.getEmail())
                .set("phone_Num", vo.getPhoneNum())
                .set("manage_Building", vo.getManageBuilding())
                .set("identity", vo.getIdentity())
                .update();
        if (!update) {
            return "更新失败！";
        }
        return null;
    }

    /**
     * 管理员添加职员
     * @param vo 职员信息
     * @return 提示
     */
    @Override
    public String managerAddStaff(StaffEditVo vo) {
        Staff staff = new Staff();
        staff.setId(vo.getId());
        staff.setGender(String.valueOf(vo.getGender()));
        staff.setName(vo.getName());
        staff.setEmail(vo.getEmail());
        staff.setPhoneNum(vo.getPhoneNum());
        staff.setManageBuilding(vo.getManageBuilding());
        staff.setIdentity(vo.getIdentity());
        boolean save = this.save(staff);
        if (!save){
            return "添加失败！";
        }
        return null;
    }

    @Override
    public Staff getStaffByBuilding(String building) {
        return this.query()
                .eq("manage_building", building)
                .one();
    }

    @Override
    public Staff getStaffById(String id) {
        return this.query()
                .eq("id", id)
                .one();
    }

    @Override
    public String updteStaffIdentity(String id, int identity) {
        boolean update = this.update()
                .eq("id", id)
                .set("identity", identity)
                .update();
        if (!update) {
            return "更新失败！";
        }
        return null;
    }
}
