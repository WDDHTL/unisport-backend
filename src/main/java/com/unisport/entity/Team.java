package com.unisport.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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
@TableName("teams")
public class Team {

    private Long id;
    private Long categoryId;
    private String name;
    private String logo;
    private String description;
    private Long captainId;
    private String department;
    private LocalDateTime createdAt;
}
