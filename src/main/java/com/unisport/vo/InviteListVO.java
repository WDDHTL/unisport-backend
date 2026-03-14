package com.unisport.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 邀请广场列表返回对象。
 */
@Data
public class InviteListVO {

    private Long id;

    private Long hostId;

    private String hostName;

    private String hostAvatar;

    private Long schoolId;

    private Long categoryId;

    private String title;

    private String description;

    private LocalDate activityDate;

    private LocalTime activityTime;

    private String location;

    private Integer maxPlayers;

    private Integer joinedCount;

    private String status;

    private String shareToken;

    private LocalDateTime createdAt;

    /**
     * 当前用户是否已加入（活跃成员）。
     */
    private Boolean isJoined;
}
