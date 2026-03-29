package com.unisport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * Status response for operations.
 */
@Data
public class WechatExchangeStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "最新状态", example = "accepted")
    private String status;
}
