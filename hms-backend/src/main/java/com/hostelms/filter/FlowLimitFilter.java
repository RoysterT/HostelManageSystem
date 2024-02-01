/**
 * FlowLimitFilter 是一个请求流量限制的过滤器类。
 *
 * 这个过滤器用于限制来自同一 IP 地址的请求频率，防止恶意或过于频繁的请求。
 */
package com.hostelms.filter;

import com.hostelms.entity.RestBean;
import com.hostelms.utils.Const;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Order(Const.ORDER_LIMIT)
public class FlowLimitFilter extends HttpFilter {

    @Resource
    StringRedisTemplate template;

    /**
     * 对传入的 HTTP 请求进行过滤，限制同一 IP 地址的请求频率。
     *
     * @param request  传入的 HTTP 请求对象
     * @param response HTTP 响应对象
     * @param chain    过滤器链，用于传递请求到下一个过滤器或目标资源
     * @throws IOException      如果在处理请求或响应时发生 I/O 错误
     * @throws ServletException 如果在处理请求时发生 Servlet 异常
     */
    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        String address = request.getRemoteAddr();
        if (this.tryCount(address)) {
            chain.doFilter(request, response);
        } else {
            this.writeBlockMessage(response);
        }
    }

    /**
     * 向响应中写入请求被阻止的消息。
     *
     * @param response HTTP 响应对象
     * @throws IOException 如果在向响应写入消息时发生 I/O 错误
     */
    private void writeBlockMessage(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(RestBean.forbidden("操作频繁，请稍后再试").asJsonString());
    }

    /**
     * 尝试计数请求的次数并检查是否应该限制请求。
     *
     * @param ip 请求的 IP 地址
     * @return 如果请求未被限制，则返回 true；否则返回 false
     */
    private boolean tryCount(String ip) {
        synchronized (ip.intern()) {
            if (Boolean.TRUE.equals(template.hasKey(Const.FLOW_LIMIT_BLOCK + ip))) {
                return false;
            }
            return this.limitPeriodCheck(ip);
        }
    }

    /**
     * 执行请求频率限制的检查。
     *
     * @param ip 请求的 IP 地址
     * @return 如果请求未被限制，则返回 true；否则返回 false
     */
    private boolean limitPeriodCheck(String ip) {
        if (Boolean.TRUE.equals(template.hasKey(Const.FLOW_LIMIT_COUNTER + ip))) {
            long increment = Optional.ofNullable(template.opsForValue().increment(Const.FLOW_LIMIT_COUNTER + ip)).orElse(0L);
            System.out.println("来自 " + ip + " 的请求，短时间内请求次数：" + increment);
            if (increment > 20) {
                // 封禁10秒
                template.opsForValue().set(Const.FLOW_LIMIT_BLOCK + ip, "", 10, TimeUnit.SECONDS);
                System.out.println("已将 IP：" + ip + " 封禁");
                return false;
            }
        } else {
            // 1秒存在时间
            template.opsForValue().set(Const.FLOW_LIMIT_COUNTER + ip, "1", 1, TimeUnit.SECONDS);
        }
        return true;
    }
}
