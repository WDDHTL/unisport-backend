package com.unisport.Enum;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * Status for wechat exchange requests.
 */
public enum WechatExchangeStatus implements IEnum<String> {
    PENDING("pending"),
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    CANCELLED("cancelled"),
    EXPIRED("expired");

    private final String value;

    WechatExchangeStatus(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
