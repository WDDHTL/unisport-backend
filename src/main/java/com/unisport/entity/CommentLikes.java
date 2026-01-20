package com.unisport.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
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
 * @since 2026/1/19$
 */
@Data
@TableName("comment_likes")
public class CommentLikes implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long commentId;
    private Long userId;
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
