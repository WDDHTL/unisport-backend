package com.unisport.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户主页信息展示VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户主页信息")
public class UserProfileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", example = "2")
    private Long id;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "学校名称", example = "清华大学")
    private String school;

    @Schema(description = "学校ID", example = "1")
    private Long schoolId;

    @Schema(description = "学院名称", example = "经管学院")
    private String department;

    @Schema(description = "个人简介", example = "篮球爱好者")
    private String bio;

    @Schema(description = "粉丝数量", example = "120")
    private Long followersCount;

    @Schema(description = "关注数量", example = "50")
    private Long followingCount;

    @Schema(description = "帖子数量", example = "35")
    private Long postsCount;

    @JsonProperty("isFollowing")
    @Schema(description = "当前登录用户是否已关注该用户", example = "false")
    private boolean following;
}
