package com.unisport.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 邀请成员列表返回对象，包含基础用户信息。
 */
@Data
public class InviteMemberDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String role;

    private String status;

    private LocalDateTime joinedAt;

    private LocalDateTime leftAt;

    private String nickname;

    private String avatar;
}
