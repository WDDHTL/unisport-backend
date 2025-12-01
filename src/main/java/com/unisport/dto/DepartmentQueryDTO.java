package com.unisport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 获取学院列表请求DTO
 * 用于接收查询学院列表的筛选条件
 *
 * @author UniSport Team
 */
@Data
@Schema(description = "获取学院列表请求对象")
public class DepartmentQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学校ID（必填）
     */
    @Schema(description = "学校ID", example = "1", required = true)
    @NotNull(message = "学校ID不能为空")
    private Long schoolId;
}
