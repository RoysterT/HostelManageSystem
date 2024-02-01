package com.hostelms.service;

import com.hostelms.entity.dto.Repair;
import org.springframework.stereotype.Service;

@Service
public interface RepairService {
    String applyNewRepair(Repair repair);

    String getRepairByOrder(int repairOrder);

    String updateRepair(Repair repair);

    String closeRepair(int[] params);

    String confirmRepair(int repairOrder);

    String finishRepair(Object[] params);

    String confirmFinish(int repairOrder);
}
