package com.unisport.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 教育经历展示对象
 */
@Data
@Schema(description = "教育经历")
public class EducationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "教育经历ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "学校名称", example = "清华大学")
    private String school;

    @Schema(description = "学校ID", example = "1")
    private Long schoolId;

    @Schema(description = "学院名称", example = "计算机系")
    private String department;

    @Schema(description = "学院ID", example = "1")
    private Long departmentId;

    @Schema(description = "学号", example = "2021123456")
    private String studentId;

    @Schema(description = "开始时间(YYYY-MM)", example = "2021-09")
    private String startDate;

    @Schema(description = "结束时间(YYYY-MM)，null 表示至今", example = "2025-06")
    private String endDate;

    @JsonProperty("isPrimary")
    @Schema(description = "是否为主要教育经历", example = "true")
    private boolean primary;

    @Schema(description = "验证状态：pending/verified/failed", example = "verified")
    private String status;

    @Schema(description = "创建时间", example = "2025-12-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "最新登录token，schoolId已更新", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
}
