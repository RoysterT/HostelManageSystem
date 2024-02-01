/**
 * JwtAuthorizeFilter 是一个用于处理 JWT（JSON Web Token）授权的过滤器。
 *
 * 这个过滤器会在每个请求中检查是否包含有效的 JWT，如果存在有效的 JWT，它将创建用户认证信息并将其存储到 Spring Security 的上下文中。
 */
package com.hostelms.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.hostelms.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthorizeFilter extends OncePerRequestFilter {

    @Resource
    JwtUtils utils;

    /**
     * 对传入的 HTTP 请求进行 JWT 授权处理。
     *
     * @param request      传入的 HTTP 请求对象
     * @param response     HTTP 响应对象
     * @param filterChain  过滤器链，用于传递请求到下一个过滤器或目标资源
     * @throws ServletException 如果在处理请求时发生 Servlet 异常
     * @throws IOException      如果在处理请求或响应时发生 I/O 错误
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        DecodedJWT jwt = utils.resolveJwt(authorization);
        if (jwt != null) {
            UserDetails userDetails = utils.toUser(jwt);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("id", utils.toId(jwt));
        }
        filterChain.doFilter(request, response);
    }
}
