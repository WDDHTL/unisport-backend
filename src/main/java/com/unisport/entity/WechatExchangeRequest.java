package com.unisport.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.unisport.Enum.WechatExchangeStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * WeChat exchange request entity.
 */
@Data
@TableName("wechat_exchange_requests")
public class WechatExchangeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long requesterId;

    private Long targetId;

    private WechatExchangeStatus status;

    private String source;

    private String requesterWechatSnapshot;

    private String targetWechatSnapshot;

    /**
     * Optional message when target rejects or responds.
     */
    private String respondMessage;

    private LocalDateTime expiredAt;

    private LocalDateTime respondedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
