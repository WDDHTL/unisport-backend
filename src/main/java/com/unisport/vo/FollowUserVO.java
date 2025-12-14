package com.unisport.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 关注列表中的用户信息展示对象。
 */
@Data
public class FollowUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String nickname;

    private String avatar;

    private Long schoolId;

    private String school;

    private String department;

    private String bio;

    private Integer gender;

    /**
     * 当前登录用户是否已关注该用户。
     */
    @JsonProperty("isFollowing")
    private boolean following;
}
