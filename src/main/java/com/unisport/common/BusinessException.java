package com.unisport.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务异常类
 */
@Getter
@AllArgsConstructor
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    public BusinessException(String message) {
        this(500, message);
    }
}
