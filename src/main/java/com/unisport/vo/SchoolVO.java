package com.unisport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 学校信息响应VO
 * 用于返回学校基本信息
 *
 * @author UniSport Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学校信息响应对象")
public class SchoolVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学校ID
     */
    @Schema(description = "学校ID", example = "1")
    private Long id;

    /**
     * 学校名称
     */
    @Schema(description = "学校名称", example = "清华大学")
    private String name;

    /**
     * 学校代码
     */
    @Schema(description = "学校代码", example = "THU")
    private String code;

    /**
     * 省份
     */
    @Schema(description = "省份", example = "北京")
    private String province;

    /**
     * 城市
     */
    @Schema(description = "城市", example = "北京市")
    private String city;
}
