package com.unisport.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 邀请成员关系表。
 */
@Data
@TableName("invite_members")
public class InviteMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long inviteId;

    private Long userId;

    /**
     * 角色 host/member。
     */
    private String role;

    /**
     * 成员状态：active/left。
     */
    private String status;

    private LocalDateTime joinedAt;

    private LocalDateTime leftAt;
}
