package com.hostelms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hostelms.entity.dto.Identity;
import com.hostelms.mapper.IdentityMapper;
import com.hostelms.service.IdentityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IdentityServiceImpl extends ServiceImpl<IdentityMapper, Identity> implements IdentityService {
    /**
     * 通过用户组序号获取用户组信息
     *
     * @param id 用户组序号
     * @return 用户组信息
     */
    @Override
    public Identity getIdentityById(int id) {
        return this.query()
                .eq("id", id)
                .one();
    }

    /**
     * 通过用户组名称获取用户组信息
     *
     * @param name 用户组名称
     * @return 用户组信息
     */
    @Override
    public Identity getIdentityByName(String name) {
        return this.query()
                .eq("name", name)
                .one();
    }

    @Override
    public List<Identity> getIdentityList() {
        return this.list();
    }

    /**
     * 通过用户组序号删除用户组信息
     *
     * @param id 用户组序号
     * @return 删除结果
     */
    @Override
    public String removeIdentityById(int id) {
        boolean remove = this.removeById(id);
        return remove ? null : "删除失败";
    }

    /**
     * 通过用户组序号更新用户组信息
     *
     * @param identity 用户组信息
     * @return 更新结果
     */
    @Override
    public String updateIdentityById(Identity identity) {
        boolean update = this.update()
                .eq("id", identity.getId())
                .set("name", identity.getName())
                .set("user_info", identity.isUserInfo())
                .set("user_edit", identity.isUserEdit())
                .set("user_password", identity.isUserPassword())
                .set("user_email", identity.isUserEmail())
                .set("user_phone", identity.isUserPhone())
                .set("accommodation_info", identity.isAccommodationInfo())
                .set("accommodation_apply", identity.isAccommodationApply())
                .set("accommodation_audit", identity.isAccommodationAudit())
                .set("accommodation_confirm", identity.isAccommodationConfirm())
                .set("inspect_rules", identity.isInspectRules())
                .set("inspect_score", identity.isInspectScore())
                .set("inspect_mark", identity.isInspectMark())
                .set("inspect_activity", identity.isInspectActivity())
                .set("maintenance_apply", identity.isMaintenanceApply())
                .set("maintenance_history", identity.isMaintenanceHistory())
                .set("maintenance_building", identity.isMaintenanceBuilding())
                .set("maintenance_manage", identity.isMaintenanceManage())
                .set("payment_bill", identity.isPaymentBill())
                .set("payment_history", identity.isPaymentHistory())
                .set("payment_building", identity.isPaymentBuilding())
                .set("payment_manage", identity.isPaymentManage())
                .set("water_bill", identity.isWaterBill())
                .set("water_history", identity.isWaterHistory())
                .set("water_sending", identity.isWaterSending())
                .set("water_manage", identity.isWaterManage())
                .set("system_permission", identity.isSystemPermission())
                .set("system_assignment", identity.isSystemAssignment())
                .set("manage_account", identity.isManageAccount())
                .set("manage_student", identity.isManageStudent())
                .set("manage_staff", identity.isManageStaff())
                .set("manage_accommodation", identity.isManageAccommodation())
                .update();
        return update ? null : "更新失败";
    }

    @Override
    public String addIdentity(Identity identity) {
        boolean save = this.save(identity);
        return save ? null : "添加失败";
    }
}
