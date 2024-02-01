package com.hostelms.controller.exception;

import com.hostelms.entity.RestBean;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ValidationController {

    /**
     * 全局异常处理器，用于捕获 ValidationException 异常并返回相应的错误响应。
     *
     * @param exception ValidationException 异常对象
     * @return 包含错误信息的 RestBean
     */
    @ExceptionHandler(ValidationException.class)
    public RestBean<Void> validateException(ValidationException exception) {
        log.warn("处理 [{}: {}] 异常", exception.getClass(), exception.getMessage());

        // 返回包含错误信息的 RestBean，状态码为 400，表示客户端请求参数有误
        return RestBean.failure(400, "请求参数有误");
    }
}
