package com.hostelms.entity.vo.response;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ChangeAccRespVo {
    int recordOrder;
    String studentIdA;
    String studentNameA;
    String buildingA;
    String unitA;
    String roomA;
    String buildingManagerA;
    String studentIdB;
    String studentNameB;
    String buildingB;
    String unitB;
    String roomB;
    String buildingManagerB;
    String description;
    String creationTime;
    String finishTime;
    int progress;
}
