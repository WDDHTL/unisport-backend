package com.unisport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 学院信息响应VO
 * 用于返回学院基本信息
 *
 * @author UniSport Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学院信息响应对象")
public class DepartmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学院ID
     */
    @Schema(description = "学院ID", example = "1")
    private Long id;

    /**
     * 学校ID
     */
    @Schema(description = "学校ID", example = "1")
    private Long schoolId;

    /**
     * 学院名称
     */
    @Schema(description = "学院名称", example = "计算机系")
    private String name;

    /**
     * 学院代码
     */
    @Schema(description = "学院代码", example = "CS")
    private String code;
}
