package com.unisport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * DTO for accepting a wechat exchange.
 */
@Data
@Schema(description = "Accept wechat exchange request body")
public class WechatExchangeAcceptDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Target user's wechat id, optional", example = "wx_bob_123")
    @Size(max = 128, message = "微信号长度不能超过128个字符")
    private String targetWechatId;
}
