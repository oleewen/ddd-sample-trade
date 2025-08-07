package com.transformer.es.service;

import com.google.common.collect.Lists;
import com.transformer.es.client.ElasticSearchClient;
import com.transformer.es.client.ElasticSearchIndex;
import com.transformer.es.service.annotation.Id;
import com.transformer.es.service.annotation.Index;
import com.transformer.es.service.model.DeepResponse;
import com.transformer.es.service.model.PageQuery;
import com.transformer.es.service.model.Pager;
import com.zto.titans.common.util.JsonUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
public abstract class BaseSearchService<Document> {
    /**
     * ES 索引名称
     */
    protected String indexes;
    /**
     * 结果实体类
     */
    protected Class<Document> documentClass;

    /**
     * 主键名称
     */
    protected java.lang.reflect.Field idField;

    protected java.lang.reflect.Field indexField;


    @Autowired
    protected ElasticSearchClient elasticSearchClient;

    public BaseSearchService() {
        initGenericClass();
        initIndexes();
        initIdField();
    }

    public BaseSearchService(String indexes) {
        this();
        this.indexes = indexes;
    }

    private void initGenericClass() {
        final Type type = this.getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            final Type[] types = pType.getActualTypeArguments();
            if (types.length == 1) {
                documentClass = (Class) types[0];
            } else {
                throw new RuntimeException("泛型参数类型缺失：" + type);
            }
        }
    }

    private void initIdField() {
        final java.lang.reflect.Field[] fields = documentClass.getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                this.idField = field;
                break;
            }
        }
    }

    private void initIndexes() {
        final boolean indexPresent = documentClass.isAnnotationPresent(Index.class);
        if (indexPresent) {
            final Index index = documentClass.getAnnotation(Index.class);
            this.indexes = index.value();
        }
        final java.lang.reflect.Field[] fields = documentClass.getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            if (field.isAnnotationPresent(Index.class)) {
                field.setAccessible(true);
                this.indexField = field;
                break;
            }
        }
    }


    @SneakyThrows
    public String create(Document doc) {
        final java.lang.reflect.Field cacheField = this.idField;
        if (cacheField != null) {
            final String id = (String) cacheField.get(doc);
            if (StringUtils.isBlank(id)) {
                return "主键对应值为空";
            }
            EsServiceUtils.emptyDateInit(doc);

            return this.creates(Lists.newArrayList(doc), true);
        }
        return "主键不明确";
    }

    public String creates(List<Document> docs, boolean immediately) {
        List<ElasticSearchIndex> indexList = index(docs);
        if (immediately) {
            return elasticSearchClient.batchInsertImmediately(indexList);
        }
        return elasticSearchClient.batchInsert(indexList);
    }

    @SneakyThrows
    private List<ElasticSearchIndex> index(List<Document> documents) {
        if (this.idField == null) {
            throw new IllegalArgumentException("主键未维护");
        }

        if (CollectionUtils.isEmpty(documents)) {
            throw new IllegalArgumentException("输入文档为空");
        }

        List<ElasticSearchIndex> indexList = Lists.newArrayList();
        for (Document doc : documents) {
            EsServiceUtils.emptyDateInit(doc);
            final String id = (String) this.idField.get(doc);
            if (StringUtils.isBlank(id)) {
                continue;
            }
            String indexStr = null;
            if (this.indexField != null) {
                indexStr = (String) this.indexField.get(doc);
            }
            ElasticSearchIndex index = new ElasticSearchIndex(getIndexOrDefault(indexStr));
            index.setId(id);
            index.setJsonData(JsonUtil.toJSON(doc));

            indexList.add(index);
        }
        return indexList;
    }

    protected String getIndexOrDefault(String index) {
        return StringUtils.isNotBlank(index) ? index : indexes;
    }


    public String update(Document doc) {
        return updates(Lists.newArrayList(doc));
    }

    public String update(Document doc, long seqNo, long primaryTerm) {
        List<ElasticSearchIndex> elasticSearchIndexList = index(Collections.singletonList(doc));
        ElasticSearchIndex index = elasticSearchIndexList.get(0);
        return elasticSearchClient.update(index.getIndexName(), index.getId(), seqNo, primaryTerm, index.getJsonData());
    }


    public String updates(List<Document> docs) {
        List<ElasticSearchIndex> elasticSearchIndexList = index(docs);
        return elasticSearchClient.batchUpdate(elasticSearchIndexList);
    }

    public String delete(String id) {
        return deletes(Lists.newArrayList(id), true);
    }

    public String deletes(List<String> ids, boolean immediately) {
        if (immediately) {
            return elasticSearchClient.batchDeleteImmediately(getIndexOrDefault(null), ids);
        }
        return elasticSearchClient.batchDelete(getIndexOrDefault(null), ids);
    }

    @SneakyThrows
    public Document query(String id) {
        final GetRequest request = new GetRequest(getIndexOrDefault(null), id);
        final GetResponse response = elasticSearchClient.getClient().get(request, RequestOptions.DEFAULT);
        final boolean exists = response.isExists();
        if (exists) {
            final String json = response.getSourceAsString();
            final Document result = JsonUtil.parse(json, documentClass);
            return result;
        }
        return null;
    }

    @SneakyThrows
    public GetResponse queryRaw(String id) {
        final GetRequest request = new GetRequest(getIndexOrDefault(null), id);
        return elasticSearchClient.getClient().get(request, RequestOptions.DEFAULT);
    }

    @SneakyThrows
    public <Query extends PageQuery> long count(Query query) {
        // 查询条件
        final CountRequest request = new CountRequest(getIndexOrDefault(null));
        final SearchSourceBuilder builder = this.searchSourceBuilder(query);
        request.source(builder);

        final CountResponse response = elasticSearchClient.getClient().count(request, RequestOptions.DEFAULT);

        return response.getCount();
    }

    @SneakyThrows
    public <Query extends PageQuery> Pager<Document, Object> query(Query query) {
        SearchRequest searchRequest = new SearchRequest(getIndexOrDefault(null));
        final SearchSourceBuilder searchSourceBuilder = this.searchSourceBuilder(query);

        // 分页
        int pageSize = (query.getPageSize() == null || query.getPageSize() == 0) ? 25 : query.getPageSize();
        int pageNumber = (query.getPageNum() == null || query.getPageNum() == 0) ? 1 : query.getPageNum();
        searchSourceBuilder
                .from((pageNumber - 1) * pageSize)
                .size(pageSize);
        // 排序
        this.sort(query, searchSourceBuilder);

        searchRequest.source(searchSourceBuilder);


        log.info("ESnativeQuery:{}", searchRequest.source().toString());
        System.out.println(searchRequest.source().toString());

        // 结果
        SearchResponse response = elasticSearchClient.getClient().search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] searchHits = response.getHits().getHits();
        long total = response.getHits().getTotalHits().value;
        List<Document> esPoList = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            String json = searchHit.getSourceAsString();
            esPoList.add(JsonUtil.parse(json, documentClass));
        }
        // 结果处理
        for (Document esPo : esPoList) {
            EsServiceUtils.emptyDateRecover(esPo);
        }
        // 封装
        final Pager<Document, Object> pageInfo = new Pager<>();
        pageInfo.setList(esPoList);
        pageInfo.setTotal(total);
        return pageInfo;
    }


    public <Query extends PageQuery> DeepResponse<Document> deepOne(Query query) {
        String scrollId = query.getScrollId();
        if (StringUtils.isEmpty(scrollId)) {
            return this.getPartitionDeepForNoScrollId(query);
        }
        return this.getPartitionDeepForHasScrollId(query);
    }

    public <Query extends PageQuery> List<Document> deepQuery(Query query) {
        List<Document> list = Lists.newArrayList();
        DeepResponse<Document> deepResponse = this.deepOne(query);
        while (deepResponse.getDataList().size() > 0) {
            list.addAll(deepResponse.getDataList());
            query.setScrollId(deepResponse.getScrollId());
            deepResponse = this.deepOne(query);
        }
        return list;
    }

    public abstract <Query extends PageQuery> void sort(Query query, SearchSourceBuilder builder);

    /**
     * 分页滚动查询 无 scrollId
     *
     * @return
     * @throws IOException
     */
    @SneakyThrows
    private <Query extends PageQuery> DeepResponse<Document> getPartitionDeepForNoScrollId(Query query) {
        List<Document> esPoList = new ArrayList<>();
        DeepResponse deepResponse = new DeepResponse();
        BoolQueryBuilder boolQueryBuilderLast = this.queryConditionAssembling(query, false);

        SearchRequest searchRequest = new SearchRequest(getIndexOrDefault(null));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilderLast);
        searchRequest.source(searchSourceBuilder);
        // 受DUBBO数据传输限制最大8M影响 这里3000条数据大约7M左右
        searchRequest.source().size(3000);
        // 排序
        //this.sort(query, searchSourceBuilder);

        searchRequest.scroll(TimeValue.timeValueMinutes(1L)); //设定滚动时间间隔

        log.info("ESnativeQuery:{}", searchRequest.source().toString());
        System.out.println(searchRequest.source().toString());

        SearchResponse searchResponse = elasticSearchClient.getClient().search(searchRequest, RequestOptions.DEFAULT);

        String scrollId = searchResponse.getScrollId();

        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit : searchHits) {
            String s = searchHit.getSourceAsString();
            final Document esPo = JsonUtil.parse(s, documentClass);
            esPoList.add(esPo);
        }
        // 结果处理
        for (Document esPo : esPoList) {
            EsServiceUtils.emptyDateRecover(esPo);
        }
        deepResponse.setScrollId(scrollId);
        deepResponse.setDataList(esPoList);
        return deepResponse;
    }

    /**
     * 分页滚动查询 存在 scrollId
     *
     * @return
     * @throws IOException
     */
    @SneakyThrows
    private <Query extends PageQuery> DeepResponse<Document> getPartitionDeepForHasScrollId(Query query) {
        String scrollId = query.getScrollId();
        List<Document> esPoList = new ArrayList<>();
        DeepResponse deepResponse = new DeepResponse();
        SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
        scrollRequest.scroll(TimeValue.timeValueMinutes(1L));
        SearchResponse searchResponse = elasticSearchClient.getClient().scroll(scrollRequest, RequestOptions.DEFAULT);
        scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        // 有数据 -> 取数据
        if (searchHits != null && searchHits.length > 0) {
            for (SearchHit searchHit : searchHits) {
                String s = searchHit.getSourceAsString();
                Document planPriceEs = JsonUtil.parse(s, documentClass);
                esPoList.add(planPriceEs);
            }
        } else { // 没数据 -> 清理资源
            // 清除滚屏
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            // 也可以选择setScrollIds()将多个scrollId一起使用
            clearScrollRequest.addScrollId(scrollId);
            // 清理资源
            elasticSearchClient.getClient().clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
            deepResponse.setLastPage(true);
        }
        deepResponse.setDataList(esPoList);
        deepResponse.setScrollId(scrollId);

        return deepResponse;
    }


    @SneakyThrows
    public <T, Query> List<T> summaryTerm(Query query, Class<T> aggClazz) {
        SearchRequest searchRequest = new SearchRequest(getIndexOrDefault(null));
        final SearchSourceBuilder searchSourceBuilder = this.searchSourceBuilder(query);
        searchSourceBuilder.size(0);
        // 聚合
        if (aggClazz != null) {
            final Collection<java.lang.reflect.Field> fields = EsServiceUtils.getCacheSimpleFields(aggClazz);
            final Pair<List<AggregationBuilder>, List<PipelineAggregationBuilder>> aggregate = EsServiceUtils.aggregate(aggClazz, query);
            for (AggregationBuilder builder : aggregate.getLeft()) {
                searchSourceBuilder.aggregation(builder);
            }
            for (PipelineAggregationBuilder builder : aggregate.getRight()) {
                searchSourceBuilder.aggregation(builder);
            }
        }
        log.warn("ES Search Condition :{}", searchSourceBuilder);
        searchRequest.source(searchSourceBuilder);
        // 结果
        SearchResponse response = elasticSearchClient.getClient().search(searchRequest, RequestOptions.DEFAULT);

        //聚合结果
        List<T> data = Lists.newArrayList();
        if (aggClazz != null) {
            data = EsServiceUtils.dealTermAgg(response.getAggregations(), aggClazz, documentClass);
        }

        return data;
    }


    /**
     * 普通查询
     *
     * @param query
     * @param <Query>
     * @return
     */
    protected <Query> SearchSourceBuilder searchSourceBuilder(Query query) {
        BoolQueryBuilder boolQueryBuilderLast = this.queryConditionAssembling(query, false);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.trackTotalHits(true);
        searchSourceBuilder.query(boolQueryBuilderLast);
        return searchSourceBuilder;
    }

    /**
     * 查询条件组装
     *
     * @param query
     * @param aggFilter
     * @param <T>
     * @return
     */
    protected <T> BoolQueryBuilder queryConditionAssembling(T query, boolean aggFilter) {
        return EsServiceUtils.queryConditionAssembling(query, aggFilter);
    }

}