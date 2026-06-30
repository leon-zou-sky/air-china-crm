package com.airchina.crm.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页返回结果
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 当前页数据 */
    private List<T> records;

    /** 当前页码 */
    private long pageNum;

    /** 每页大小 */
    private long pageSize;

    /** 总页数 */
    private long pages;

    public PageResult() {
        this.records = Collections.emptyList();
    }

    public PageResult(long total, List<T> records, long pageNum, long pageSize) {
        this.total = total;
        this.records = records;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (total + pageSize - 1) / pageSize;
    }

    /**
     * 从 MyBatis-Plus 分页结果转换
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
    }
}
