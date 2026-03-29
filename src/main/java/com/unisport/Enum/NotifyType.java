package com.unisport.Enum;

import com.baomidou.mybatisplus.annotation.IEnum;

public enum NotifyType implements IEnum<String> {
    LIKE("like"),
    COMMENT("comment"),
    FOLLOW("follow"),
    SYSTEM("system"),
    WECHAT_EXCHANGE_REQUEST("wechat_exchange_request"),
    WECHAT_EXCHANGE_ACCEPT("wechat_exchange_accept"),
    WECHAT_EXCHANGE_REJECT("wechat_exchange_reject");

    private final String value;

    NotifyType(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value; // 这个值会写入数据库
    }
}
