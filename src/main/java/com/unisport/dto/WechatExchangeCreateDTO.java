package com.unisport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * DTO for creating a wechat exchange request.
 */
@Data
@Schema(description = "Create wechat exchange request body")
public class WechatExchangeCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Target user id", example = "2002")
    @JsonProperty("target_id")
    @NotNull(message = "目标用户ID不能为空")
    private Long targetId;

    @Schema(description = "Source scene marker", example = "profile")
    @Size(max = 32, message = "来源标记长度不能超过32个字符")
    private String source;
}
