package com.unisport.config;

import com.unisport.common.BusinessException;
import com.unisport.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "请求参数错误";
        log.error("参数校验失败: {}", message);
        return Result.error(400, message);
    }

    /**
     * 参数绑定异常处理
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "请求参数错误";
        log.error("参数绑定失败: {}", message);
        return Result.error(400, message);
    }

    /**
     * 资源或接口不存在
     */
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public Result<?> handleNotFound(Exception e) {
        String path = null;
        if (e instanceof NoResourceFoundException resourceEx) {
            path = resourceEx.getResourcePath();
        } else if (e instanceof NoHandlerFoundException handlerEx) {
            path = handlerEx.getRequestURL();
        }
        log.warn("未找到资源: {}", path);
        return Result.error(404, path == null ? "请求资源不存在" : "请求资源不存在: " + path);
    }

    /**
     * 未知异常兜底
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统异常，请联系管理员");
    }
}
