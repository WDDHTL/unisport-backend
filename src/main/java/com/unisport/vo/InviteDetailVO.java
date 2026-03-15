package com.unisport.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Invite detail response object containing invite info and member list.
 */
@Data
public class InviteDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private InviteListVO invite;

    private List<InviteMemberBriefVO> members;
}
