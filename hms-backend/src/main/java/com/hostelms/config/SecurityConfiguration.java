package com.hostelms.config;

import com.hostelms.entity.RestBean;
import com.hostelms.entity.dto.Account;
import com.hostelms.entity.vo.response.AuthorizeVO;
import com.hostelms.filter.JwtAuthorizeFilter;
import com.hostelms.service.AccountService;
import com.hostelms.service.StaffService;
import com.hostelms.service.StudentService;
import com.hostelms.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfiguration {

    /**
     * 工具类实例化
     */
    @Resource
    JwtUtils utils;

    /**
     * JWT过滤器实例化
     */
    @Resource
    JwtAuthorizeFilter jwtAuthorizeFilter;

    /**
     * 账户Service类实例化
     */
    @Resource
    AccountService accountService;

    /**
     * 学生Service类实例化
     */
    @Resource
    StudentService studentService;

    /**
     * 职员Service类实例化
     */
    @Resource
    StaffService staffService;

    /**
     * 配置Spring Security安全过滤器链
     *
     * @param http http请求
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 配置对请求的授权规则
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests
                                // 允许所有用户访问账户认证路径
                                .requestMatchers("/api/auth/**").permitAll()
                                // 其他请求需要身份验证
                                .anyRequest().authenticated()
                )
                // 配置表单登录相关设置
                .formLogin(formLogin -> formLogin
                        // 登录页面路径
                        .loginPage("/api/auth/login")
                        .permitAll()
                        // 配置登录处理的 URL
                        .loginProcessingUrl("/api/auth/login")
                        // 配置登录失败和成功的处理方法
                        .failureHandler(this::onAuthenticationFailure)
                        .successHandler(this::onAuthenticationSuccess)
                )
                // 登出相关设置
                .logout(logout -> logout
                        // 登出 URL
                        .logoutUrl("/api/auth/logout")
                        // 登出成功后的处理方法
                        .logoutSuccessHandler(this::onLogoutSuccess)
                )
                // 配置异常处理
                .exceptionHandling(conf -> conf
                        // 未认证用户访问受保护资源的处理方法
                        .authenticationEntryPoint(this::onUnauthentication)
                        // 访问被拒绝资源的处理方法
                        .accessDeniedHandler(this::onAccessDeny)
                )
                // 禁用 CSRF 保护
                .csrf(AbstractHttpConfigurer::disable)
                // 配置会话管理策略为无状态
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 在 UsernamePasswordAuthenticationFilter 之前添加自定义的 JWT 认证过滤器
                .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * 处理访问被拒绝的情况，返回 JSON 格式的错误响应
     */
    public void onAccessDeny(HttpServletRequest request,
                             HttpServletResponse response,
                             AccessDeniedException exception) throws IOException, ServletException {
        // 设置响应内容类型为 JSON 格式
        response.setContentType("application/json");
        // 设置响应字符编码为 UTF-8
        response.setCharacterEncoding("utf-8");

        // 获取异常信息，并将其转换为 JSON 格式的字符串，然后写入响应
        response.getWriter().write(RestBean.forbidden(exception.getMessage()).asJsonString());
    }

    /**
     * 处理身份验证成功的情况，返回 JSON 格式的成功响应，包含 JWT 令牌和用户信息。
     */
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 设置响应内容类型为 JSON 格式
        response.setContentType("application/json");
        // 设置响应字符编码为 UTF-8
        response.setCharacterEncoding("utf-8");

        // 从身份验证信息中获取用户信息
        User user = (User) authentication.getPrincipal();

        // 根据用户名查找用户账户信息
        Account account = accountService.findAccountById(user.getUsername());
        String studentTrueName = studentService.getTrueNameById(user.getUsername());
        String trueName = studentTrueName != null ? studentTrueName : staffService.getTrueNameById(user.getUsername());

        // 创建 JWT 令牌
        String token = utils.createJwt(
                user,
                account.getId(),
                trueName);

        // 创建 AuthorizeVO 对象，包含令牌、过期时间、用户角色和用户名
        AuthorizeVO vo = new AuthorizeVO();
        vo.setExpire(utils.expireTime());
        vo.setRole(account.getIdentity());
        vo.setToken(token);
        vo.setUsername(trueName);
        vo.setId(user.getUsername());

        // 将 AuthorizeVO 对象转换为 JSON 格式的字符串，并写入响应
        response.getWriter().write(RestBean.success(vo).asJsonString());
    }

    /**
     * 处理身份验证失败的情况，返回 JSON 格式的未授权响应
     */
    public void onUnauthentication(HttpServletRequest request,
                                   HttpServletResponse response,
                                   AuthenticationException exception) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(RestBean.unauthorized(exception.getMessage()).asJsonString());
    }

    /**
     * 处理身份验证失败的情况，返回 JSON 格式的身份验证失败响应
     */
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(RestBean.failure(401, exception.getMessage()).asJsonString());
    }

    /**
     * 处理成功退出登录的情况，返回 JSON 格式的成功退出登录响应或失败响应
     */
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter writer = response.getWriter();
        // 从请求头中获取 Authorization 字段的值
        String authorization = request.getHeader("Authorization");

        // 检查 Authorization 字段的值是否有效
        if (utils.invaliddateJwt(authorization)) {
            // 如果有效，返回成功的退出登录响应
            writer.write(RestBean.success().asJsonString());
        }
        else {
            // 如果无效，返回退出登录失败的响应
            writer.write(RestBean.failure(400, "退出登录失败").asJsonString());
        }
    }
}