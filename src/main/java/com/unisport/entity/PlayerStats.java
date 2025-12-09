package com.unisport.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2025/12/8$
 */
@Data
@TableName("player_stats")
public class PlayerStats implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long leagueId;

    private Long teamMemberId;

    private Long teamId;

    private Integer played;

//    @TableField("`stat_value`")
    private Integer statValue;

    private Integer assists;

//    @TableField("`player_rank`")
    private Integer playerRank;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
