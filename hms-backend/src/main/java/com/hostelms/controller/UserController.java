package com.hostelms.controller;

import com.hostelms.entity.RestBean;
import com.hostelms.entity.vo.request.ChangeEmailVo;
import com.hostelms.entity.vo.request.ChangePhoneVo;
import com.hostelms.service.AccommodationService;
import com.hostelms.service.AccountService;
import com.hostelms.service.StaffService;
import com.hostelms.service.StudentService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    AccountService service;

    @Resource
    private StudentService studentService;

    @Resource
    private StaffService staffService;

    @Resource
    private AccommodationService accommodationService;

    @Resource
    private MessageHandle message;

    /**
     * 获取学生信息
     */
    @GetMapping("/stu-info")
    public String getStudentInfo(@RequestParam("id") String id) {
        return studentService.findStudentById(id);
    }

    /**
     * 获取员工信息
     */
    @GetMapping("/staff-info")
    public String getStaffInfo(@RequestParam("id") String id) {
        return staffService.findStaffById(id);
    }

    /**
     * 修改邮箱
     */
    @PostMapping("/change-email")
    public RestBean<Void> changeEmail(@RequestBody @Valid ChangeEmailVo vo){
        return message.messageHandle(vo, service::changeEmail);
    }

    /**
     * 修改手机号
     */
    @PostMapping("/change-phone")
    public RestBean<Void> changePhone(@RequestBody @Valid ChangePhoneVo vo){
        if (vo.getIdentity()==3){
            return message.messageHandle(vo, studentService::changePhone);
        }
        else {
            return message.messageHandle(vo, staffService::changePhone);
        }
    }

    @GetMapping("/accommodation")
    public String getUserAccommodation(@RequestParam("id") String id) {
        return accommodationService.getUserAccommodation(id);
    }
}

