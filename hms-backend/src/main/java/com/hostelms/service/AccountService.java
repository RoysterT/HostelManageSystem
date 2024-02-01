package com.hostelms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hostelms.entity.dto.Account;
import com.hostelms.entity.vo.request.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public interface AccountService extends IService<Account>, UserDetailsService {
    Account findAccountById(String username);

    String registerAccount(RegisterVo vo);

    String registerEmailVerifyCode(String type, String email, String ip);

    String resetConfirm(ConfirmResetVo vo);

    String resetEmailAccountPassword(EmailResetVo vo);

    String changeEmail(ChangeEmailVo vo);

    // 获取所有账户数据
//    Account[] findAllAccount(Integer pageNum, Integer pageSize, String id, String email);

//    String editAccount(AccountEditVo vo);

    String managerResetAccountPassword(String id);

    String managerChangeAccountStatus(ChangeStatusVo vo);

    String managerDeleteAccountById(String id);

    String managerEditAccountByRegOrder(AccountEditVo vo);

    String managerAddAccount(AddAccountVo vo);
}
