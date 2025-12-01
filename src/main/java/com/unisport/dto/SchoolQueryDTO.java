package com.unisport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 获取学校列表请求DTO
 * 用于接收查询学校列表的筛选条件
 *
 * @author UniSport Team
 */
@Data
@Schema(description = "获取学校列表请求对象")
public class SchoolQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 省份筛选（可选）
     */
    @Schema(description = "省份筛选", example = "北京", required = false)
    private String province;

    /**
     * 城市筛选（可选）
     */
    @Schema(description = "城市筛选", example = "北京市", required = false)
    private String city;
}
