package com.unisport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2025/12/12$
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "联赛信息响应对象")
public class LeagueVO {
    private static final long serialVersionUID = 1L;

    /**
     * 联赛ID
     */
    private Long id;

    /**
     * 联赛名称
     */
    private String name;

    private Integer categoryId;
}
