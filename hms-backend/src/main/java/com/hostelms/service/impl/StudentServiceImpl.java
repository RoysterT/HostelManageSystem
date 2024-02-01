package com.hostelms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hostelms.entity.RestBean;
import com.hostelms.entity.dto.Student;
import com.hostelms.entity.vo.request.ChangePhoneVo;
import com.hostelms.entity.vo.request.StudentEditVo;
import com.hostelms.entity.vo.response.StudentVo;
import com.hostelms.mapper.StudentMapper;
import com.hostelms.service.StudentService;
import com.hostelms.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    @Resource
    FlowUtils utils;

    @Resource
    StudentMapper studentMapper;

    @Override
    public boolean hasStudent(String id) {
        Student data = this.query()
                .eq("id", id)
                .one();
        if (data != null) {
            return true;
        }
        return false;
    }

    /**
     * 根据用户名查找用户账户，返回json字符串。
     *
     * @param id 学号
     * @return Account 用户账户对象
     */
    @Override
    public String findStudentById(String id) {
        StudentVo vo = new StudentVo();
        Student data = this.query()
                .eq("id", id)
                .one();
        vo.setId(id);
        vo.setName(data.getName());
        vo.setGender(data.getGender());
        vo.setEmail(data.getEmail());
        vo.setPhoneNum(data.getPhoneNum());
        vo.setDept(data.getDept());
        vo.setMajor(data.getMajor());
        vo.setClassId(data.getClassId());
        vo.setAdmission(data.getAdmission());
        vo.setGraduation(data.getGraduation());
        return RestBean.success(vo).asJsonString();
    }

    /**
     * 根据学生 ID 获取学生实体
     *
     * @param id 学生 ID
     * @return Student 学生实体
     */
    @Override
    public Student getStudentById(String id) {
        return this.query()
                .eq("id", id)
                .one();
    }

    /**
     * 根据学生 ID 获取姓名。
     *
     * @param id 学生 ID
     * @return String 姓名，如果找到则返回姓名，否则返回 null
     */
    @Override
    public String getTrueNameById(String id) {
        // 先从 student 表中查询学生姓名
        QueryWrapper<Student> studentQuery = new QueryWrapper<>();
        studentQuery.eq("id", id);
        Student student = studentMapper.selectOne(studentQuery);

        if (student != null) {
            return student.getName();
        }
        return null;
    }

    @Override
    public String changePhone(ChangePhoneVo vo) {
        boolean update = this.update()
                .eq("id", vo.getId())
                .set("phone_Num", vo.getPhone())
                .update();
        if (!update) {
            return "更新失败！";
        }
        return null;
    }

    /**
     * 更新学生数据表邮箱
     *
     * @param id    学生学号
     * @param email 新邮箱
     */
    @Override
    public void updateEmail(String id, String email) {
        boolean update = this.update()
                .eq("id", id)
                .set("email", email)
                .update();
        if (!update) {
        }
    }

    /**
     * 根据学生 ID 删除学生
     *
     * @param id 学生 ID
     * @return String 删除结果
     */
    @Override
    public String managerDeleteStudentById(String id) {
        boolean remove = this.removeById(id);
        if (!remove) {
            return "删除失败！";
        }
        return null;
    }

    /**
     * 根据学生 ID 更新学生信息
     *
     * @param vo 学生信息
     * @return String 更新结果
     */
    @Override
    public String managerEditStudentById(StudentEditVo vo) {
        boolean update = this.update()
                .eq("id", vo.getId())
                .set("name", vo.getName())
                .set("gender", vo.getGender())
                .set("email", vo.getEmail())
                .set("phone_Num", vo.getPhoneNum())
                .set("dept", vo.getDept())
                .set("major", vo.getMajor())
                .set("class_id", vo.getClassId())
                .set("admission", vo.getAdmission())
                .set("graduation", vo.getGraduation())
                .update();
        if (!update) {
            return "更新失败！";
        }
        return null;
    }

    /**
     * 添加学生
     * @param vo 学生信息
     * @return String 添加结果
     */
    @Override
    public String managerAddStudent(StudentEditVo vo) {
        Student student = new Student();
        student.setId(vo.getId());
        student.setName(vo.getName());
        student.setGender(String.valueOf(vo.getGender()));
        student.setEmail(vo.getEmail());
        student.setPhoneNum(vo.getPhoneNum());
        student.setDept(vo.getDept());
        student.setMajor(vo.getMajor());
        student.setClassId(vo.getClassId());
        student.setAdmission(vo.getAdmission());
        student.setGraduation(vo.getGraduation());
        boolean save = this.save(student);
        if (!save) {
            return "添加失败！";
        }
        return null;
    }
}
