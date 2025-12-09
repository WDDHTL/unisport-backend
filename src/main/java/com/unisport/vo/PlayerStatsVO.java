package com.unisport.vo;

import lombok.Data;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2025/12/8$
 */
@Data
public class PlayerStatsVO {
    private static final long serialVersionUID = 1L;

    /**
     * 排名
     */
    private Integer rank;

    /**
     * 队伍/用户名称
     */
    private String playerName;

    private String teamName;

    private Integer played;

    private Integer goals;
}
