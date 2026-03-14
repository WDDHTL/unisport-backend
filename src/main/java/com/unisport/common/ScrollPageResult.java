package com.unisport.common;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 滚动分页结果封装
 */
@Data
public class ScrollPageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 本次返回的记录
     */
    private List<T> records = Collections.emptyList();

    /**
     * 本次请求条数
     */
    private long size;

    /**
     * 是否还有更多数据
     */
    private boolean hasMore;

    /**
     * 下一次请求的游标时间
     */
    private LocalDateTime nextCursorTime;

    /**
     * 下一次请求的游标ID
     */
    private Long nextCursorId;

    public static <T> ScrollPageResult<T> of(List<T> records,
                                             long size,
                                             boolean hasMore,
                                             LocalDateTime nextCursorTime,
                                             Long nextCursorId) {
        ScrollPageResult<T> r = new ScrollPageResult<>();
        r.setRecords(records == null ? Collections.emptyList() : records);
        r.setSize(size);
        r.setHasMore(hasMore);
        r.setNextCursorTime(nextCursorTime);
        r.setNextCursorId(nextCursorId);
        return r;
    }
}
