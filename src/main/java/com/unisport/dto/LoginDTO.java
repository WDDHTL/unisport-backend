package com.unisport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求DTO
 * 
 * 用于接收前端传递的登录参数
 *
 * @author UniSport Team
 */
@Data
@Schema(description = "用户登录请求参数")
public class LoginDTO {

    /**
     * 账号（学号/手机号）
     * 必填项，不能为空
     */
    @NotBlank(message = "账号不能为空")
    @Schema(description = "账号（学号/手机号）", example = "2024001", required = true)
    private String account;

    /**
     * 密码
     * 必填项，不能为空，前端传递明文密码，后端验证时与加密密码比对
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456", required = true)
    private String password;
}
