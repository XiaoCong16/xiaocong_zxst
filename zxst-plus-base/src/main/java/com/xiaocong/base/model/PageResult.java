package com.xiaocong.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    // 数据列表
    private List<T> items;

    //总记录数
    private long counts;

    //当前页码
    private long page;

    //每页记录数
    private long pageSize;

//    public PageResult(List<T> items, long counts, long page, long pageSize) {
//        this.items = items;
//        this.counts = counts;
//        this.page = page;
//        this.pageSize = pageSize;
//    }
}
