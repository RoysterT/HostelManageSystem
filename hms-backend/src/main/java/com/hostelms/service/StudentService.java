package com.hostelms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hostelms.entity.dto.Student;
import com.hostelms.entity.vo.request.ChangePhoneVo;
import com.hostelms.entity.vo.request.StudentEditVo;
import org.springframework.stereotype.Service;

@Service
public interface StudentService extends IService<Student> {
    boolean hasStudent(String id);

    String findStudentById(String id);

    Student getStudentById(String id);

    String getTrueNameById(String id);

    String changePhone(ChangePhoneVo vo);

    void updateEmail(String id, String email);

    String managerDeleteStudentById(String id);

    String managerEditStudentById(StudentEditVo vo);

    String managerAddStudent(StudentEditVo vo);
}
