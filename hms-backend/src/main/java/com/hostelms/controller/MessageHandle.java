package com.hostelms.controller;

import com.hostelms.entity.RestBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Function;
import java.util.function.Supplier;

@RestController
public class MessageHandle {
    /**
     * 处理返回信息
     * @param vo 业务逻辑
     * @param function 业务逻辑
     * @return 返回信息
     * @param <T>
     */
    public <T> RestBean<Void> messageHandle(T vo, Function<T, String> function) {
        return messageHandle(() -> function.apply((vo)));
    }

    /**
     * 处理返回信息
     * @param action 业务逻辑
     * @return 返回信息
     */
    public RestBean<Void> messageHandle(Supplier<String> action) {
        String message = action.get();
        return message == null ? RestBean.success() : RestBean.failure(400, message);
    }
}
