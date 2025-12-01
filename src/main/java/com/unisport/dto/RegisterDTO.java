package com.unisport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求DTO
 * 用于接收前端注册表单数据
 *
 * @author UniSport Team
 */
@Data
@Schema(description = "用户注册请求对象")
public class RegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账号（学号/手机号）
     */
    @Schema(description = "账号（学号/手机号）", example = "2024001")
    @NotBlank(message = "账号不能为空")
    @Size(min = 3, max = 50, message = "账号长度必须在3-50个字符之间")
    private String account;

    /**
     * 密码
     */
    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    /**
     * 学校名称
     */
    @Schema(description = "学校名称", example = "清华大学")
    @NotBlank(message = "学校名称不能为空")
    @Size(max = 100, message = "学校名称长度不能超过100个字符")
    private String school;

    /**
     * 学校ID
     */
    @Schema(description = "学校ID", example = "1")
    @NotNull(message = "学校ID不能为空")
    private Long schoolId;

    /**
     * 学院名称
     */
    @Schema(description = "学院名称", example = "计算机系")
    @NotBlank(message = "学院名称不能为空")
    @Size(max = 100, message = "学院名称长度不能超过100个字符")
    private String department;

    /**
     * 学院ID
     */
    @Schema(description = "学院ID", example = "1")
    @NotNull(message = "学院ID不能为空")
    private Long departmentId;

    /**
     * 学号
     */
    @Schema(description = "学号", example = "2024001001")
    @NotBlank(message = "学号不能为空")
    @Size(max = 30, message = "学号长度不能超过30个字符")
    private String studentId;
}
