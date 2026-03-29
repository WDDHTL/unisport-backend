package com.unisport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * Simplified user info for embedding.
 */
@Data
public class SimpleUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", example = "1001")
    private Long id;

    @Schema(description = "昵称", example = "Alice")
    private String nickname;

    @Schema(description = "头像", example = "https://example.com/avatar.jpg")
    private String avatar;
}
