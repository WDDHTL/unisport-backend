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
 * @since 2026/1/12$
 */
@Data
@TableName("post_likes")
public class PostLikes implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long postId;
    private Long userId;
    private LocalDateTime createdAt;
}
