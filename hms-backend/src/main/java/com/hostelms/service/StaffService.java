package com.hostelms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hostelms.entity.dto.Staff;
import com.hostelms.entity.vo.request.ChangePhoneVo;
import com.hostelms.entity.vo.request.StaffEditVo;
import org.springframework.stereotype.Service;

@Service
public interface StaffService extends IService<Staff> {
    int getStaffIdentity(String id);

    String findStaffById(String id);

    String getTrueNameById(String id);

    String changePhone(ChangePhoneVo vo);

    String updateEmail(String id, String email);

    String managerDeleteStaffById(String id);

    String managerEditStaffById(StaffEditVo vo);

    String managerAddStaff(StaffEditVo vo);

    Staff getStaffByBuilding(String building);

    Staff getStaffById(String id);

    String updteStaffIdentity(String id, int identity);
}
