package com.unisport.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户注册响应VO
 * 用于返回注册成功后的用户信息
 *
 * @author UniSport Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户注册响应对象")
public class RegisterVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long id;

    /**
     * 账号
     */
    @Schema(description = "账号", example = "2024001")
    private String account;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "2024001")
    private String nickname;

    /**
     * 学号
     */
    @Schema(description = "学号", example = "2024001001")
    private String studentId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-12-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
