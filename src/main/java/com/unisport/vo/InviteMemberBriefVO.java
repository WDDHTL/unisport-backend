package com.unisport.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Invite member brief info for invite detail view.
 */
@Data
public class InviteMemberBriefVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String role;

    private String status;

    private LocalDateTime joinedAt;

    private LocalDateTime leftAt;
}
