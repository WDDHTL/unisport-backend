package com.unisport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新用户信息请求DTO
 */
@Data
@Schema(description = "更新用户信息请求对象")
public class UpdateUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "张三丰")
    @Size(min = 1, max = 50, message = "昵称长度需在1-50个字符之间")
    private String nickname;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatar;

    /**
     * 个人简介
     */
    @Schema(description = "个人简介", example = "热爱运动")
    @Size(max = 200, message = "个人简介长度不能超过200个字符")
    private String bio;

    /**
     * 性别：1-男，0-女
     */
    @Schema(description = "性别：1-男，0-女", example = "1")
    @Min(value = 0, message = "性别取值仅支持0或1")
    @Max(value = 1, message = "性别取值仅支持0或1")
    private Integer gender;
}
