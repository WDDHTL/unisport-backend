package com.unisport.Enum;

import com.baomidou.mybatisplus.annotation.IEnum;

public enum RelatedType implements IEnum<String> {
    POST("post"),
    COMMENT("comment"),
    USER("user"),
    MATCH("match");

    private final String value;

    RelatedType(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value; // 写入数据库的字符串
    }
}
