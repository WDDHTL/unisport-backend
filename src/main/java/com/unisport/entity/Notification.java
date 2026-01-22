package com.unisport.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unisport.Enum.NotifyType;
import com.unisport.Enum.RelatedType;
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
@TableName("notifications")
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long senderId;
    private NotifyType type;
    private RelatedType relatedType;
    private Long postId;
    private Long relatedId;
    private String content;
    private int isRead;
    private LocalDateTime createdAt;
}
