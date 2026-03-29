package com.unisport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * DTO for rejecting a wechat exchange request.
 */
@Data
@Schema(description = "Reject wechat exchange request body")
public class WechatExchangeRejectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Optional reject reason", example = "暂时不方便交换")
    @Size(max = 255, message = "原因长度不能超过255个字符")
    private String reason;
}
