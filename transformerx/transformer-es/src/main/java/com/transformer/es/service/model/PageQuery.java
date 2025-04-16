package com.transformer.es.service.model;


import java.io.Serializable;

public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer pageNum;
    private Integer pageSize;
    private String scrollId;

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }
}
