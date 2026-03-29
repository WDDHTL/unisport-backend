package com.unisport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Wechat exchange request view.
 */
@Data
public class WechatExchangeRequestVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "请求ID", example = "301")
    private Long id;

    @Schema(description = "状态: pending/accepted/rejected/cancelled/expired", example = "pending")
    private String status;

    @Schema(description = "发起人信息")
    private SimpleUserVO requester;

    @Schema(description = "接收人信息")
    private SimpleUserVO target;

    @Schema(description = "来源标记", example = "profile")
    private String source;

    @Schema(description = "对方微信号，仅双方且已accepted时返回", example = "wx_bob_123")
    private String otherWechatId;

    @Schema(description = "响应说明（拒绝等）", example = "暂时不方便")
    private String respondMessage;

    @Schema(description = "过期时间")
    private LocalDateTime expiredAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "处理时间")
    private LocalDateTime respondedAt;
}
