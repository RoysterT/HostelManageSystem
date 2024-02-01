package com.hostelms.controller;

import com.hostelms.entity.RestBean;
import com.hostelms.entity.vo.request.ConfirmResetVo;
import com.hostelms.entity.vo.request.EmailResetVo;
import com.hostelms.entity.vo.request.RegisterVo;
import com.hostelms.service.AccountService;
import com.hostelms.service.StaffService;
import com.hostelms.service.StudentService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Stack;
import java.util.function.Function;
import java.util.function.Supplier;

@Validated
@RestController
@RequestMapping ("/api/auth")
public class AuthorizeController {

    @Resource
    AccountService service;

    @Resource
    StudentService studentService;

    @Resource
    StaffService staffService;

    @Resource
    MessageHandle message;

    /**
     * 发送验证码请求
     *
     * @param email   目标邮箱地址
     * @param type    验证码类型（"register" 或 "reset"）
     * @param request HttpServletRequest 请求对象
     * @return 包含操作结果的 RestBean
     */
    @GetMapping ("/ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam @Email String email,
                                        @RequestParam @Pattern (regexp = "(register|reset|change)") String type,
                                        HttpServletRequest request) {
        return message.messageHandle(() ->
                service.registerEmailVerifyCode(type, email, request.getRemoteAddr()));
    }

    /**
     * 用户注册请求
     */
    @PostMapping ("/register")
    public RestBean<Void> register(@RequestBody @Valid RegisterVo vo) {
        if (studentService.hasStudent(vo.getId())){
            vo.setIdentity(3);
            studentService.updateEmail(vo.getId(), vo.getEmail());
        }
        else {
            vo.setIdentity(staffService.getStaffIdentity(vo.getId()));
            staffService.updateEmail(vo.getId(), vo.getEmail());
        }
        return message.messageHandle(vo, service::registerAccount);
    }

    /**
     * 重置密码确认请求
     */
    @PostMapping ("/reset-confirm")
    public RestBean<Void> resetConfirm(@RequestBody @Valid ConfirmResetVo vo) {
        return message.messageHandle(vo, service::resetConfirm);
    }

    /**
     * 重置密码请求
     */
    @PostMapping ("/reset-password")
    public RestBean<Void> resetConfirm(@RequestBody @Valid EmailResetVo vo) {
        return message.messageHandle(vo, service::resetEmailAccountPassword);
    }
}
