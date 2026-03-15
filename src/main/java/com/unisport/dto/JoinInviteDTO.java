package com.unisport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 加入邀请请求体
 */
@Data
@Schema(description = "加入邀请请求")
public class JoinInviteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "加入备注", example = "一起踢球")
    @Size(max = 200, message = "备注长度不能超过200字符")
    private String comment;

    @Schema(description = "来源分享token", example = "ABCD1234")
    @Size(max = 32, message = "分享token长度不能超过32字符")
    private String fromShareToken;
}
