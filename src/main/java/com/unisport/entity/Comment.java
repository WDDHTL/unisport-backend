package com.unisport.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * $ 服务实现类
 * </p>
 *
 * @author 86139$
 * @since 2026/1/14$
 */
@Data
@TableName("comments")
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long postId;
    private Long userId;
    private Long parentId;
    private String content;
    private Integer likesCount;
    private Integer deleted;
    private LocalDateTime createdAt;
}
