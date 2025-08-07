package com.transformer.es.service;


import com.transformer.es.service.model.DeepResponse;
import com.transformer.es.service.model.PageQuery;
import com.transformer.es.service.model.Pager;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.List;

public interface SearchService<Query extends PageQuery, Document, Aggregate> {

    /**
     * 保存
     *
     * @param document 文档
     * @return
     */
    String create(Document document);

    /**
     * 批量保存
     *
     * @param documents   文档
     * @param immediately 是否立即
     * @return
     */
    String creates(List<Document> documents, boolean immediately);

    /**
     * 更新
     *
     * @param document 文档
     * @return
     */
    String update(Document document);
    /**
     * 乐观锁更新
     *
     * @param doc 文档
     * @return
     */
    String update(Document doc,long seqNo,long primaryTerm);

    /**
     * 批量更新
     *
     * @param documents 文档
     * @return
     */
    String updates(List<Document> documents);

    /**
     * 删除
     *
     * @param id 文档id
     * @return
     */
    String delete(String id);

    /**
     * 批量删除
     *
     * @param ids         文档id
     * @param immediately 是否立即
     * @return
     */
    String deletes(List<String> ids, boolean immediately);

    /**
     * 单条查询
     *
     * @param id 文档id
     * @return
     */
    Document query(String id);

    GetResponse queryRaw(String id);
    /**
     * 分页查询
     *
     * @param query     查询条件
     * @param aggregate 是否聚合
     * @return
     */
    Pager<Document, Aggregate> query(Query query, boolean aggregate);

    /**
     * 滚动查询全量
     *
     * @param query
     * @return
     */
    List<Document> deepQuery(Query query);

    /**
     * 滚动查询 单次
     *
     * @param query
     * @return
     */
    DeepResponse<Document> deepOne(Query query);

    /**
     * 自动区分 普通查询 和 深度查询
     *
     * @param query
     * @return
     */
    List<Document> autoQuery(Query query);

    /**
     * 聚合查询
     *
     * @param query
     * @return
     */
    Aggregate summary(Query query);

    /**
     * 分组汇总
     *
     * @param query
     * @return
     */
    List<Aggregate> summaryTerm(Query query);

    <T>List<T> summaryTerm(Query query, Class<T> aggClazz);

    /**
     * 自定义汇总
     *
     * @param query
     * @return
     */
    List<Aggregate> summaryCustom(Query query);

    /**
     * 总条数查询
     *
     * @param query 查询条件
     * @return
     */
    long count(Query query);

    /**
     * 排序
     *
     * @param query   过滤条件
     * @param builder
     */
    void sort(Query query, SearchSourceBuilder builder);
}
