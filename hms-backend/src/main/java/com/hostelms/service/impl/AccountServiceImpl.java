/**
 * AccountServiceImpl 是 AccountService 接口的实现类，用于提供与账户相关的服务。
 *
 * 这个类使用了 Spring Framework 的注解，标识为一个服务类，以便进行依赖注入和组件扫描。
 */
package com.hostelms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hostelms.entity.dto.Account;
import com.hostelms.entity.vo.request.*;
import com.hostelms.mapper.AccountMapper;
import com.hostelms.service.AccountService;
import com.hostelms.utils.Const;
import com.hostelms.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Resource
    FlowUtils utils;

    @Resource
    PasswordEncoder encoder;

    @Resource
    AmqpTemplate amqpTemplate;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    /**
     * 从数据库加载用户信息，用于 Spring Security 认证。
     *
     * @param id 用户名
     * @throws UsernameNotFoundException 如果未找到用户账号
     */
    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Account account = this.findAccountById(id);
        if (account == null) {
            throw new UsernameNotFoundException("未找到用户账号！");
        }
        return User
                .withUsername(id)
                .password(account.getPassword())
                .roles(String.valueOf(account.getIdentity()))
                .build();
    }

    /**
     * 生成并发送邮箱验证码，用于注册或密码重置。
     *
     * @param type 注册或重置密码的类型
     * @param email 邮箱地址
     * @param ip 请求的 IP 地址
     * @return String 验证码生成结果，如果生成成功则返回 null，否则返回错误消息
     */
    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()) {
            if (!this.verifyLimit(ip)) {
                return "请求频繁，请稍后再试";
            }
            // 产生6位验证码
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            amqpTemplate.convertAndSend("mail", data);
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code), Const.CODE_VALID_TIME, TimeUnit.SECONDS);
            return null;
        }
    }

    /**
     * 验证 IP 请求频率限制。
     *
     * @param ip 请求的 IP 地址
     * @return boolean 如果请求未超过频率限制则返回 true，否则返回 false
     */
    private boolean verifyLimit(String ip) {
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        return utils.limitOnceCheck(key, Const.ASK_EMAIL_TIME);
    }

    /**
     * 注册用户账户。
     *
     * @param vo RegisterVo 对象，包含注册信息
     * @return String 注册结果，如果注册成功则返回 null，否则返回错误消息
     */
    @Override
    public String registerAccount(RegisterVo vo) {
        String email = vo.getEmail();
        String key = Const.VERIFY_EMAIL_DATA + email;
        String code = stringRedisTemplate.opsForValue().get(key);
        String id = vo.getId();
        int identity = vo.getIdentity();
        if (code == null) {
            return "请先获取验证码";
        }
        if (!code.equals(vo.getCode())) {
            return "验证码错误，请重新输入";
        }
        if (this.existAccountById(id)) {
            return "该用户已注册，请直接登录";
        }
        else if (this.existAccountByEmail(email)) {
            return "该邮箱已注册，请直接登录";
        }
        String password = encoder.encode(vo.getPassword());
        Account account = new Account(null, id, email, password, true, identity);
        if (this.save(account)) {
            stringRedisTemplate.delete(key);
            return null;
        }
        else {
            return "内部错误，请联系管理员";
        }
    }

    /**
     * 重置密码前的邮箱认证。
     *
     * @param vo ConfirmResetVo 对象，包含邮箱和验证码信息
     * @return String 验证结果，如果验证成功则返回 null，否则返回错误消息
     */
    @Override
    public String resetConfirm(ConfirmResetVo vo) {
        String email = vo.getEmail();
        String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA + email);
        if (code == null) {
            return "请先获取验证码";
        }
        if (!code.equals(vo.getCode())) {
            return "验证码错误，请重新输入";
        }
        return null;
    }

    /**
     * 重置邮箱账户密码。
     *
     * @param vo EmailResetVo 对象，包含邮箱和新密码信息
     * @return String 重置密码结果，如果成功则返回 null，否则返回错误消息
     */
    @Override
    public String resetEmailAccountPassword(EmailResetVo vo) {
        String email = vo.getEmail();
        String verify = this.resetConfirm(new ConfirmResetVo(email, vo.getCode()));
        if (verify != null) {
            return verify;
        }
        String password = encoder.encode(vo.getPassword());
        boolean update = this.update()
                .eq("email", email)
                .set("password", password)
                .update();
        if (update) {
            stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);
        }
        return null;
    }

    /**
     * 更改账户邮箱
     * @param vo ChangeEmailVo 对象，包括账号id、新邮箱和验证码
     * @return
     */
    @Override
    public String changeEmail(ChangeEmailVo vo) {
        String email = vo.getEmail();
        String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA + email);
        if (code == null) {
            return "请先获取验证码";
        }
        if (!code.equals(vo.getCode())) {
            return "验证码错误，请重新输入";
        }
        boolean update = this.update()
                .eq("id",vo.getId())
                .set("email",email)
                .update();
        if (!update){
            return "更新失败！";
        }
        return null;
    }

    /**
     * 获取所有账户信息
     *
     * @return String 所有账户信息
     */
    public Account[] findAllAccount(Integer pageNum, Integer pageSize, String id, String email) {
//        Account[] accounts = this.list().toArray(new Account[0]);
//        String accountsJson = JSONObject.toJSONString(accounts);
//        // 返回所有账户信息
//        return accountsJson;
        return this.list().toArray(new Account[0]);
    }

    /**
     * 管理员修改账户信息
     */
    /*@Override
    public String editAccount(AccountEditVo vo) {
        String password = encoder.encode(vo.getPassword());
        boolean update = this.update()
                .eq("id",vo.getId())
                .set("email",vo.getEmail())
                .set("status",vo.getStatus())
                .set("password",password)
                .set("identity",vo.getIdentity())
                .update();
        if (!update){
            return "更新失败！";
        }
        return null;
    }*/

    /**
     * 根据用户名查找用户账户。
     *
     * @param id 用户名
     * @return Account 用户账户对象
     */
    @Override
    public Account findAccountById(String id) {
        return this.query()
                .eq("id", id)
                .one();
    }

    /**
     * 根据id查找用户账户
     * @param id 用户id
     * @return 删除成功返回true，否则返回false
     */
    public boolean removeById(String id) {
        return this.removeById(this.query().eq("id", id).one().getRegOrder());
    }

    /**
     * 检查邮箱是否已经存在于数据库中。
     *
     * @param email 邮箱地址
     * @return Boolean 如果邮箱已存在则返回 true，否则返回 false
     */
    public Boolean existAccountByEmail(String email) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email", email));
    }

    /**
     * 检查id是否已经存在于数据库中。
     *
     * @param id 学号 / 工号
     * @return Boolean 如果用户名已存在则返回 true，否则返回 false
     */
    public Boolean existAccountById(String id) {
        return this.baseMapper.exists(Wrappers.<Account>query().eq("id", id));
    }

    /**
     * 重置账户密码
     * @param id 账户id
     * @return String 重置密码结果，如果成功则返回 null，否则返回错误消息
     */
    @Override
    public String managerResetAccountPassword(String id) {
        // 重置为id后6位
        String password = encoder.encode(id.substring(id.length()-6));
        boolean update = this.update()
                .eq("id",id)
                .set("password",password)
                .update();
        if (!update){
            return "更新失败！";
        }
        return null;
    }

    /**
     * 管理员修改账户状态
     * @param vo ChangeStatusVo 对象，包括账户id和新状态
     * @return String 修改状态结果，如果成功则返回 null，否则返回错误消息
     */
    @Override
    public String managerChangeAccountStatus(ChangeStatusVo vo) {
        boolean update = this.update()
                .eq("id",vo.getId())
                .set("status",vo.getStatus())
                .update();
        if (!update){
            return "更新失败！";
        }
        return null;
    }

    /**
     * 管理员删除账户
     * @param id 账户id
     * @return String 删除结果，如果成功则返回 null，否则返回错误消息
     */
    @Override
    public String managerDeleteAccountById(String id) {
        boolean delete = this.removeById(id);
        if (!delete){
            return "删除失败！";
        }
        return null;
    }

    @Override
    public String managerEditAccountByRegOrder(AccountEditVo vo) {
        boolean update = this.update()
                .eq("reg_order",vo.getRegOrder())
                .set("id",vo.getId())
                .set("email",vo.getEmail())
                .set("identity",vo.getIdentity())
                .update();
        if (!update){
            return "更新失败！";
        }
        return null;
    }

    @Override
    public String managerAddAccount(AddAccountVo vo) {
        String id = vo.getId();
        String email = vo.getEmail();
        String password = encoder.encode(id.substring(id.length()-6));
        // 默认状态为true，否则按照vo中的状态
        Boolean status = vo.getStatus() == null || vo.getStatus();
        int identity = vo.getIdentity();
        Account account = new Account(null, id, email, password, status, identity);
        if (this.save(account)) {
            return null;
        }
        else {
            return "内部错误，请联系管理员";
        }
    }
}
