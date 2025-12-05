package com.unisport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录响应VO
 * 
 * 登录成功后返回给前端的数据，包含JWT Token和用户基本信息
 *
 * @author UniSport Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录响应数据")
public class LoginVO {

    /**
     * JWT Token令牌
     * 前端需要存储在localStorage中，后续请求携带在Authorization头中
     */
    @Schema(description = "JWT Token令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    /**
     * 用户基本信息
     */
    @Schema(description = "用户基本信息")
    private UserInfo user;

    /**
     * 用户基本信息内部类
     * 包含用户ID、昵称和头像
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户基本信息")
    public static class UserInfo {

        /**
         * 用户ID
         */
        @Schema(description = "用户ID", example = "1")
        private Long id;

        /**
         * 用户昵称
         */
        @Schema(description = "用户昵称", example = "张三")
        private String nickname;

        /**
         * 用户头像URL
         */
        @Schema(description = "用户头像URL", example = "https://example.com/avatar.jpg")
        private String avatar;
    }
}
