package com.unisport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 添加教育经历请求DTO
 */
@Data
@Schema(description = "添加教育经历请求体")
public class AddEducationDTO {

    @NotNull(message = "学校ID不能为空")
    @Schema(description = "学校ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long schoolId;

    @NotNull(message = "学院ID不能为空")
    @Schema(description = "学院ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long departmentId;

    @NotBlank(message = "学号不能为空")
    @Schema(description = "学号", example = "2021123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String studentId;

    @NotBlank(message = "开始时间不能为空")
    @Schema(description = "开始时间(YYYY-MM)", example = "2021-09", requiredMode = Schema.RequiredMode.REQUIRED)
    private String startDate;

    @Schema(description = "结束时间(YYYY-MM)，null表示至今", example = "2025-06")
    private String endDate;
}
