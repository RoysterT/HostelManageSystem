package com.hostelms.entity.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@Data
@TableName("Identity")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Identity {
    int id;
    String name;
    boolean userInfo;
    boolean userEdit;
    boolean userPassword;
    boolean userEmail;
    boolean userPhone;
    boolean accommodationInfo;
    boolean accommodationApply;
    boolean accommodationAudit;
    boolean accommodationConfirm;
    boolean inspectRules;
    boolean inspectScore;
    boolean inspectMark;
    boolean inspectActivity;
    boolean maintenanceApply;
    boolean maintenanceHistory;
    boolean maintenanceBuilding;
    boolean maintenanceManage;
    boolean paymentBill;
    boolean paymentHistory;
    boolean paymentBuilding;
    boolean paymentManage;
    boolean waterBill;
    boolean waterHistory;
    boolean waterSending;
    boolean waterManage;
    boolean systemPermission;
    boolean systemAssignment;
    boolean manageAccount;
    boolean manageStudent;
    boolean manageStaff;
    boolean manageAccommodation;
}
