package com.unisport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * Query dto for listing wechat exchange requests.
 */
@Data
@Schema(description = "Wechat exchange request query params")
public class WechatExchangeListQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Role: received/sent", example = "received")
    private String role;

    @Schema(description = "Status filter, comma separated", example = "pending,accepted")
    private String status;

    @Schema(description = "Current page number", example = "1")
    private Integer current;

    @Schema(description = "Page size", example = "10")
    private Integer size;
}
