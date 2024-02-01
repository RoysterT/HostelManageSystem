package com.hostelms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hostelms.entity.RestBean;
import com.hostelms.entity.dto.Accommodation;
import com.hostelms.entity.dto.Account;
import com.hostelms.entity.dto.Staff;
import com.hostelms.entity.dto.Student;
import com.hostelms.entity.vo.request.*;
import com.hostelms.mapper.AccommodationMapper;
import com.hostelms.mapper.AccountMapper;
import com.hostelms.mapper.StaffMapper;
import com.hostelms.mapper.StudentMapper;
import com.hostelms.service.AccommodationService;
import com.hostelms.service.AccountService;
import com.hostelms.service.StaffService;
import com.hostelms.service.StudentService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/manage")
public class ManageController {

    @Resource
    private AccountService service;

    @Resource
    private StudentService studentService;

    @Resource
    private StaffService staffService;

    @Resource
    private AccommodationService accommodationService;

    @Resource
    MessageHandle message;

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    StudentMapper studentMapper;

    @Autowired
    StaffMapper staffMapper;

    @Autowired
    AccommodationMapper accommodationMapper;

    @GetMapping("/account/info")
    public RestBean<Map<String, Object>> getAccountInfo(@RequestParam("page") int pageNum,
                                                        @RequestParam("pageSize") int pageSize,
                                                        @RequestParam("id") String id,
                                                        @RequestParam("email") String email) {
//        return service.findAllAccount();
//        return (RestBean) new RestBean<com.hostelms.entity.dto.Account[]>(200, service.findAllAccount(), "请求成功");
        Page<Account> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(id != null && !"undefined".equals(id), "id", id);
        queryWrapper.like(email != null && !"undefined".equals(email), "email", email);
        accountMapper.selectPage(page, queryWrapper);
        List<Account> records = page.getRecords();
        Map<String, Object> results = Map.of(
                "total", page.getTotal(),
                "currentPage", page.getCurrent(),
                "data", records);
        return new RestBean<>(200, results, "请求成功");
    }

    @GetMapping("/account/reset-password")
    public RestBean<Void> resetPassword(@RequestParam("id") String id) {
        return message.messageHandle(id, service::managerResetAccountPassword);
    }

    @GetMapping("/account/change-status")
    public RestBean<Void> changeStatus(@RequestParam("id") String id,
                                       @RequestParam("status") boolean status) {
        ChangeStatusVo vo = new ChangeStatusVo(id, status);
        return message.messageHandle(vo, service::managerChangeAccountStatus);
    }

    @GetMapping("/account/delete")
    public RestBean<Void> deleteAccount(@RequestParam("id") String id) {
        return message.messageHandle(id, service::managerDeleteAccountById);
    }

    @PostMapping("/account/add")
    public RestBean<Void> addAccount(@RequestBody @Valid AddAccountVo vo) {
        if (studentService.hasStudent(vo.getId())){
            studentService.updateEmail(vo.getId(), vo.getEmail());
        }
        else {
            staffService.updateEmail(vo.getId(), vo.getEmail());
        }
        return message.messageHandle(vo, service::managerAddAccount);
    }

    @PostMapping("/account/edit")
    public RestBean<Void> managerEditAccountByRegOrder(@RequestBody @Valid AccountEditVo vo) {
        if (vo.getIdentity() == 3 && !vo.getId().matches("^\\d{9}$")) {
            return new RestBean<>(400, null, "学号格式错误");
        } else if (vo.getIdentity() != 3 && !vo.getId().matches("^\\d{2}-\\d{3}$")) {
            return new RestBean<>(400, null, "工号格式错误");
        } else if (service.findAccountById(vo.getId()).getRegOrder() != vo.getRegOrder()) {
            if (vo.getIdentity() == 3) {
                return new RestBean<>(400, null, "学号冲突");
            }
            return new RestBean<>(400, null, "工号冲突");
        } else if (vo.getIdentity() == 3 && vo.getId().matches("^\\d{9}$") ||
                vo.getId().matches("^\\d{2}-\\d{3}$")) {
            String update = service.managerEditAccountByRegOrder(vo);
            // 是职工，则更新职工数据表的身份列
            if (staffService.getStaffById(vo.getId()) != null){
                staffService.updteStaffIdentity(vo.getId(), vo.getIdentity());
            }
            return message.messageHandle(vo, service::managerEditAccountByRegOrder);
        }
        return null;
    }

    @GetMapping("/student/info")
    public RestBean<Map<String, Object>> getStudentInfo(@RequestParam("page") int pageNum,
                                                        @RequestParam("pageSize") int pageSize,
                                                        @RequestParam("id") String id,
                                                        @RequestParam("email") String email,
                                                        @RequestParam("name") String name,
                                                        @RequestParam("phoneNum") String phoneNum,
                                                        @RequestParam("dept") String dept,
                                                        @RequestParam("major") String major) {
        Page<Student> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(id != null && !"undefined".equals(id), "id", id);
        if (!Objects.equals(email, "")){
            queryWrapper.like(email != null && !"undefined".equals(email), "email", email);
        }
        queryWrapper.like(name != null && !"undefined".equals(name), "name", name);
        if (!Objects.equals(phoneNum, "")){
            queryWrapper.like(phoneNum != null && !"undefined".equals(phoneNum), "phone_num", phoneNum);
        }
        queryWrapper.like(dept != null && !"undefined".equals(dept), "dept", dept);
        queryWrapper.like(major != null && !"undefined".equals(major), "major", major);
        studentMapper.selectPage(page, queryWrapper);
        List<Student> records = page.getRecords();
        Map<String, Object> results = Map.of(
                "total", page.getTotal(),
                "currentPage", page.getCurrent(),
                "data", records);
        return new RestBean<>(200, results, "请求成功");
    }

    @GetMapping("/student/delete")
    public RestBean<Void> deleteStudent(@RequestParam("id") String id) {
        if (service.getById(id) != null) {
            service.removeById(id);
        }
        return message.messageHandle(id, studentService::managerDeleteStudentById);
    }

    @PostMapping("/student/add")
    public RestBean<Void> addStudent(@RequestBody @Valid StudentEditVo vo) {
        return message.messageHandle(vo, studentService::managerAddStudent);
    }

    @PostMapping("/student/edit")
    public RestBean<Void> managerEditStudentById(@RequestBody @Valid StudentEditVo vo) {
        if (!vo.getId().matches("^\\d{9}$")) {
            return new RestBean<>(400, null, "学号格式错误");
        } else if (vo.getId().length() == 9) {
            return message.messageHandle(vo, studentService::managerEditStudentById);
        }
        return null;
    }

    @GetMapping("/staff/info")
    public RestBean<Map<String, Object>> getStaffInfo(@RequestParam("page") int pageNum,
                                                      @RequestParam("pageSize") int pageSize,
                                                      @RequestParam("id") String id,
                                                      @RequestParam("name") String name,
                                                      @RequestParam("email") String email,
                                                      @RequestParam("phoneNum") String phoneNum) {
        Page<Staff> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Staff> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(id != null && !"undefined".equals(id), "id", id);
        queryWrapper.like(email != null && !"undefined".equals(email), "email", email);
        queryWrapper.like(name != null && !"undefined".equals(name), "name", name);
        if (!Objects.equals(phoneNum, "")){
            queryWrapper.like(phoneNum != null && !"undefined".equals(phoneNum), "phone_num", phoneNum);
        }
        staffMapper.selectPage(page, queryWrapper);
        List<Staff> records = page.getRecords();
        Map<String, Object> results = Map.of(
                "total", page.getTotal(),
                "currentPage", page.getCurrent(),
                "data", records);
        return new RestBean<>(200, results, "请求成功");
    }

    @GetMapping("/staff/delete")
    public RestBean<Void> deleteStaff(@RequestParam("id") String id) {
        if (service.getById(id) != null) {
            service.removeById(id);
        }
        return message.messageHandle(id, staffService::managerDeleteStaffById);
    }

    @PostMapping("/staff/add")
    public RestBean<Void> addStaff(@RequestBody @Valid StaffEditVo vo) {
        return message.messageHandle(vo, staffService::managerAddStaff);
    }

    @PostMapping("/staff/edit")
    public RestBean<Void> managerEditStaffById(@RequestBody @Valid StaffEditVo vo) {
        if (!vo.getId().matches("^\\d{2}-\\d{3}$")) {
            return new RestBean<>(400, null, "工号格式错误");
        } else if (vo.getId().length() == 6) {
            // 如果存在账户则更新账户表的身份列
            if (service.getById(vo.getId()) != null) {
                service.update().eq("id", vo.getId()).set("identity", vo.getIdentity()).update();
            }
            return message.messageHandle(vo, staffService::managerEditStaffById);
        }
        return null;
    }

    @GetMapping("/accommodation/info")
    public RestBean<Map<String, Object>> getAccommodationInfo(@RequestParam("page") int pageNum,
                                                      @RequestParam("pageSize") int pageSize,
                                                      @RequestParam("id") String id,
                                                      @RequestParam("building") String building,
                                                      @RequestParam("unit") String unit,
                                                      @RequestParam("room") String room,
                                                      @RequestParam("bed") String bed) {
        Page<Accommodation> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Accommodation> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(id != null && !"undefined".equals(id), "id", id);
        if (!Objects.equals(building, "")){
            queryWrapper.like(building != null && !"undefined".equals(building), "building", building);
        }
        if (!Objects.equals(unit, "")){
            queryWrapper.like(unit != null && !"undefined".equals(unit), "unit", unit);
        }
        if (!Objects.equals(room, "")){
            queryWrapper.like(room != null && !"undefined".equals(room), "room", room);
        }
        if (!Objects.equals(bed, "")){
            queryWrapper.like(bed != null && !"undefined".equals(bed), "bed", bed);
        }
        accommodationMapper.selectPage(page, queryWrapper);
        List<Accommodation> records = page.getRecords();
        Map<String, Object> results = Map.of(
                "total", page.getTotal(),
                "currentPage", page.getCurrent(),
                "data", records);
        return new RestBean<>(200, results, "请求成功");
    }

    @GetMapping("/accommodation/delete")
    public RestBean<Void> deleteAccommodation(@RequestParam("id") String id) {
        return message.messageHandle(id, accommodationService::managerDeleteAccommodationById);
    }

    @PostMapping("/accommodation/add")
    public RestBean<Void> addAccommodation(@RequestBody @Valid AccommodationEditVo vo) {
        return message.messageHandle(vo, accommodationService::managerAddAccommodation);
    }

    @PostMapping("/accommodation/edit")
    public RestBean<Void> managerEditAccommodationById(@RequestBody @Valid AccommodationEditVo vo) {
        if (vo.getId().length() == 9 && !vo.getId().matches("^\\d{9}$")) {
            return new RestBean<>(400, null, "学号格式错误");
        } else if (vo.getId().length() == 6 && !vo.getId().matches("^\\d{2}-\\d{3}$")) {
            return new RestBean<>(400, null, "工号格式错误");
        } else if (vo.getId().length() == 6 || vo.getId().length() == 9) {
            return message.messageHandle(vo, accommodationService::managerEditAccommodationByRegOrder);
        }
        return null;
    }
}
