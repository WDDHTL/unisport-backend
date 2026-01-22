package com.unisport.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知列表中单条通知的展示对象。
 */
@Data
public class NotificationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID。
     */
    private Long id;

    /**
     * 通知类型（like/comment/follow/system）。
     */
    private String type;

    /**
     * 触发通知的用户昵称；系统通知等无触发人时会展示占位文案。
     */
    private String userName;

    /**
     * 通知内容摘要。
     */
    private String content;

    // Related resource type, stored in notifications.related_type
    @JsonProperty("related_type")
    private String relatedType;

    // Related resource id, stored in notifications.related_id
    @JsonProperty("related_id")
    private Long relatedId;

    // Related post id, stored in notifications.post_id
    @JsonProperty("post_id")
    private Long postId;

    /**
     * 是否已读。
     */
    private Boolean isRead;

    /**
     * 通知创建时间。
     */
    private LocalDateTime createdAt;
}
