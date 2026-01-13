package com.unisport.Enum;

import com.baomidou.mybatisplus.annotation.IEnum;

public enum NotifyType implements IEnum<String> {
    LIKE("like"),
    COMMENT("comment"),
    FOLLOW("follow"),
    SYSTEM("system");

    private final String value;

    NotifyType(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value; // 这个值会写入数据库
    }
}
