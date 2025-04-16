package com.transformer.es.service.model;

import lombok.Data;

import java.util.List;


@Data
public class Pager<Doc, Aggregate> {
    private List<Doc> list;
    private long total;
    /**
     * 聚合实体
     * 1. 当不许要使用聚合时,聚合类 使用 ESEmptyAgg代替
     * 2. 当同一索引同时存在 聚合和 非聚合查询 时， 在调用 com.zto.ts.core.component.es.assist.model.EsPageInfo 时通过 needAggregate 转换
     * 2. 实体参数 返回值 默认为 ***BigDicimal**
     */
    private Aggregate aggregation;
}
