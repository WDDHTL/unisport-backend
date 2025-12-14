package com.unisport.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 简单的分页结果封装，契合前端需要的字段格式。
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页记录列表
     */
    private List<T> records = Collections.emptyList();

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页码，从1开始
     */
    private long current;

    /**
     * 每页大小
     */
    private long size;

    /**
     * 总页数
     */
    private long pages;

    public static <T> PageResult<T> of(long current, long size, long total, long pages, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setCurrent(current);
        result.setSize(size);
        result.setTotal(total);
        result.setPages(pages);
        result.setRecords(records == null ? Collections.emptyList() : records);
        return result;
    }
}
