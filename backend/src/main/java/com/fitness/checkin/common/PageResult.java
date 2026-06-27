package com.fitness.checkin.common;

import lombok.Data;

import java.util.List;

/**
 * 分页返回体
 */
@Data
public class PageResult<T> {
    private long total;
    private List<T> list;

    public PageResult(long total, List<T> list) {
        this.total = total;
        this.list = list;
    }
}
