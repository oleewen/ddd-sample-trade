package com.transformer.es.service;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.transformer.es.service.annotation.*;
import com.transformer.es.service.model.*;
import com.zto.titans.common.util.JsonUtil;
import com.transformer.es.client.ElasticSearchClient;
import com.transformer.es.client.ElasticSearchIndex;
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
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.*;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.aggregations.pipeline.ParsedSimpleValue;
import org.elasticsearch.search.aggregations.pipeline.SumBucketPipelineAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springside.modules.utils.reflect.ReflectionUtil;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractSearchService<Query extends PageQuery, Document, Aggregate> implements SearchService<Query, Document, Aggregate> {
    /**
     * ES 索引名称
     */
    protected String indexes;
    /**
     * 查询实体类
     */
    protected Class<Query> queryClass;
    /**
     * 结果实体类
     */
    protected Class<Document> documentClass;
    /**
     * 聚合实体类
     */
    protected Class<Aggregate> aggregateClass;
    /**
     * 主键名称
     */
    protected java.lang.reflect.Field idField;

    protected java.lang.reflect.Field indexField;
    /**
     * 默认空时间
     */
    public static final String DEFAULT_EMPTY_DATE = "1900-01-01 00:00:00";
    /**
     * 默认查询结果集大小
     */
    private static final int INDEX_MAX_RESULT_WINDOW = 10000;


    // @ApolloJsonValue("${es.search.latest.days:{'" + GLOBAL_LATEST_DAYS + "':-1}}")
    private HashMap<String, Integer> esSearchLatestDays;
    /**
     * 查询时间范围 根据Apollo配置调整
     */
    public static final String GLOBAL_LATEST_DAYS = "_global";

    protected Multimap<Class, java.lang.reflect.Field> dateFieldCache = ArrayListMultimap.create();
    protected Multimap<Class, java.lang.reflect.Field> simpleFieldCache = ArrayListMultimap.create();

    @Autowired
    protected ElasticSearchClient elasticSearchClient;

    public AbstractSearchService() {
        initGenericClass();

        initIndexes();

        initIdField();

        getCacheDateFields(documentClass);

        getCacheSimpleFields(documentClass);
        getCacheSimpleFields(aggregateClass);
        getCacheSimpleFields(queryClass);
    }

    public AbstractSearchService(String indexes) {
        this();
        this.indexes = indexes;
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

    private void initGenericClass() {
        final Type type = this.getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            final Type[] types = pType.getActualTypeArguments();
            if (types.length == 3) {
                queryClass = (Class) types[0];
                documentClass = (Class) types[1];

                if (types[2] instanceof ParameterizedType) {
                    final ParameterizedType aggType = (ParameterizedType) types[2];
                    final Type[] actualTypeArguments = aggType.getActualTypeArguments();
                    aggregateClass = (Class) actualTypeArguments[0];
                } else {
                    aggregateClass = (Class) types[2];
                }
            } else {
                throw new RuntimeException("泛型参数类型缺失：" + type);
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
            this.emptyDateInit(doc);

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
            this.emptyDateInit(doc);
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

    private String getIndexOrDefault(String index) {
        return StringUtils.isNotBlank(index) ? index : indexes;
    }

    @SneakyThrows
    private void emptyDateInit(Document document) {
        final Collection<java.lang.reflect.Field> cacheDateFields = getCacheDateFields(document.getClass());
        if (CollectionUtils.isEmpty(cacheDateFields)) {
            return;
        }
        for (java.lang.reflect.Field field : cacheDateFields) {
            field.setAccessible(true);
            final String o = (String) field.get(document);
            if (StringUtils.isBlank(o)) {
                field.set(document, DEFAULT_EMPTY_DATE);
            }
        }
    }

    @SneakyThrows
    private void emptyDateRecover(Document document) {
        final Collection<java.lang.reflect.Field> cacheDateFields = getCacheDateFields(document.getClass());
        if (CollectionUtils.isEmpty(cacheDateFields)) {
            return;
        }
        for (java.lang.reflect.Field field : cacheDateFields) {
            field.setAccessible(true);
            final String o = (String) field.get(document);
            if (DEFAULT_EMPTY_DATE.equals(o)) {
                field.set(document, "");
            }
        }
    }

    private Collection<java.lang.reflect.Field> getCacheSimpleFields(Class<?> clazz) {
        final Collection<java.lang.reflect.Field> fieldCollection = simpleFieldCache.get(clazz);
        if (CollectionUtils.isNotEmpty(fieldCollection)) {
            return fieldCollection;
        }
        final List<java.lang.reflect.Field> data = this.getFieldList(clazz).stream().filter(f -> f.isAnnotationPresent(Field.class)).collect(Collectors.toList());
        simpleFieldCache.putAll(clazz, data);

        return simpleFieldCache.get(clazz);
    }

    private List<java.lang.reflect.Field> getFieldList(Class<?> clazz) {
        List<java.lang.reflect.Field> fieldList = new ArrayList<>();
        while (clazz != null && !clazz.equals(Object.class)) {
            final java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
            fieldList.addAll(Arrays.asList(fields));
            clazz = clazz.getSuperclass();
        }
        return fieldList;
    }

    private Collection<java.lang.reflect.Field> getCacheDateFields(Class<?> clazz) {
        final Collection<java.lang.reflect.Field> fieldCollection = dateFieldCache.get(clazz);
        if (CollectionUtils.isNotEmpty(fieldCollection)) {
            return fieldCollection;
        }

        final java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            final boolean present = field.isAnnotationPresent(Field.class);
            if (!present) {
                continue;
            }
            final Field docField = field.getAnnotation(Field.class);
            if (docField.type().equals(FieldType.DATE)) {
                fieldCollection.add(field);
            }
        }

        return dateFieldCache.get(clazz);
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
    public long count(Query query) {
        // 查询条件
        final CountRequest request = new CountRequest(getIndexOrDefault(null));
        final SearchSourceBuilder builder = this.searchSourceBuilder(query);
        request.source(builder);

        final CountResponse response = elasticSearchClient.getClient().count(request, RequestOptions.DEFAULT);

        return response.getCount();
    }

    @SneakyThrows
    protected <T> Pair<List<AggregationBuilder>, List<PipelineAggregationBuilder>> aggregate(Class clazz, T query) {
        final ArrayList<AggregationBuilder> aggBuilders = Lists.newArrayList();
        final ArrayList<PipelineAggregationBuilder> aggPipeBuilders = Lists.newArrayList();


        final java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        final List<java.lang.reflect.Field> fieldList = Arrays.asList(fields);
        // 聚合字段
        List<java.lang.reflect.Field> aggFields = fieldList.stream().filter(f -> f.isAnnotationPresent(AggregateField.class)).collect(Collectors.toList());

        // 处理分组
        final Optional<java.lang.reflect.Field> term = fieldList.stream()
                .filter(f -> f.isAnnotationPresent(TermField.class))
                .findFirst();

        AggregationBuilder aggBuilder = null;
        if (term.isPresent()) {
            final java.lang.reflect.Field field = term.get();
            final TermField esField = field.getAnnotation(TermField.class);
            String aggName = field.getName();
            String esFieldName = esField.value();
            if (StringUtils.isBlank(esFieldName)) {
                esFieldName = aggName;
            }
            final TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms(aggName).field(esFieldName).size(esField.size());
            aggBuilder = termsAggregationBuilder;
            aggBuilders.add(termsAggregationBuilder);
        }

        // 处理子分组
        final List<java.lang.reflect.Field> subAggFields = aggFields.stream()
                .filter(f -> f.getAnnotation(AggregateField.class).aggType().equals(AggregateField.AggType.SUB_AGG))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(subAggFields)) {
            final java.lang.reflect.Field[] queryFields = query.getClass().getDeclaredFields();
            for (java.lang.reflect.Field subAggField : subAggFields) {

                final AggregateField annotation = subAggField.getAnnotation(AggregateField.class);
                Class filterClazz = annotation.filter();
                FilterAggregationBuilder filter = null;
                if (filterClazz != null && !filterClazz.equals(EmptyFilter.class)) {
                    // query 赋值 filter 中
                    Object instance = filterClazz.newInstance();
                    final java.lang.reflect.Field[] declaredFields = filterClazz.getDeclaredFields();
                    for (java.lang.reflect.Field declaredField : declaredFields) {
                        final String fieldName = declaredField.getName();
                        for (java.lang.reflect.Field queryField : queryFields) {
                            if (StringUtils.equals(fieldName, queryField.getName())) {
                                final Object value = ReflectionUtil.getFieldValue(instance, fieldName);
                                ReflectionUtil.setFieldValue(instance, declaredField.getName(), value);
                            }
                        }
                    }
                    filter = AggregationBuilders.filter(annotation.value(), queryConditionAssembling(instance, true));
                    aggBuilder.subAggregation(filter);
                }

                final ResolvableType type = ResolvableType.forField(subAggField);
                final Class subAggClazz = (Class) type.getGeneric(0).getType();
                final Pair<List<AggregationBuilder>, List<PipelineAggregationBuilder>> listPair = aggregate(subAggClazz, query);
                // 优先filter
                if (filter != null) {
                    for (AggregationBuilder builder : listPair.getLeft()) {
                        filter.subAggregation(builder);
                    }
                    for (PipelineAggregationBuilder builder : listPair.getRight()) {
                        filter.subAggregation(builder);

                    }
                    continue;
                }
                // 其次 terms
                if (aggBuilder != null) {
                    for (AggregationBuilder builder : listPair.getLeft()) {
                        aggBuilder.subAggregation(builder);
                    }
                    for (PipelineAggregationBuilder builder : listPair.getRight()) {
                        aggBuilder.subAggregation(builder);

                    }
                    continue;
                }

                for (AggregationBuilder builder : listPair.getLeft()) {
                    aggBuilders.add(builder);
                }
                for (PipelineAggregationBuilder builder : listPair.getRight()) {
                    aggPipeBuilders.add(builder);
                }

            }
        }

        // 处理聚合
        for (java.lang.reflect.Field aggField : aggFields) {
            final AggregateField esField = aggField.getAnnotation(AggregateField.class);
            if (esField.aggType() == AggregateField.AggType.SUM_BUCKETS) {
                PipelineAggregationBuilder aggregationBuilder = getPipelineAggregationBuilder(aggField);
                if (aggregationBuilder == null) {
                    continue;
                } else {
                    if (aggBuilder != null) {
                        aggBuilder.subAggregation(aggregationBuilder);
                    } else {
                        aggPipeBuilders.add(aggregationBuilder);
                    }
                }
            } else {
                AggregationBuilder aggregationBuilder = getAggregationBuilder(aggField);
                if (aggregationBuilder == null) {
                    continue;
                } else {
                    if (aggBuilder != null) {
                        aggBuilder.subAggregation(aggregationBuilder);
                    } else {
                        aggBuilders.add(aggregationBuilder);
                    }
                }
            }
        }

        return Pair.of(aggBuilders, aggPipeBuilders);
    }

    private static AggregationBuilder getAggregationBuilder(java.lang.reflect.Field aggField) {
        final AggregateField esField = aggField.getAnnotation(AggregateField.class);
        String aggName = aggField.getName();
        String esFieldName = esField.value();
        if (StringUtils.isBlank(esFieldName)) {
            esFieldName = aggName;
        }
        final AggregateField.AggType aggType = esField.aggType();
        AggregationBuilder aggregationBuilder = null;
        switch (aggType) {
            case AVG:
                aggregationBuilder = AggregationBuilders.avg(aggName).field(esFieldName);
                break;
            case SUM:
                aggregationBuilder = AggregationBuilders.sum(aggName).field(esFieldName);
                break;
            case COUNT:
                aggregationBuilder = AggregationBuilders.count(aggName).field(esFieldName);
                break;
            case MIN:
                aggregationBuilder = AggregationBuilders.min(aggName).field(esFieldName);
                break;
            case MAX:
                aggregationBuilder = AggregationBuilders.max(aggName).field(esFieldName);
                break;
            case CARDINALITY:
                aggregationBuilder = AggregationBuilders.cardinality(aggName).field(esFieldName);
                break;
            case TOP_HITS:
                final String includes = esField.includeSources();
                List<String> includeFieldSource = null;
                List<String> excludeFieldSource = null;
                if (StringUtils.isNotBlank(includes)) {
                    includeFieldSource = Splitter.on(",")
                            .omitEmptyStrings()
                            .trimResults()
                            .splitToList(includes);
                }
                final String excludes = esField.excludeSources();
                if (StringUtils.isNotBlank(excludes)) {
                    excludeFieldSource = Splitter.on(",")
                            .omitEmptyStrings()
                            .trimResults()
                            .splitToList(excludes);
                }

                final FetchSourceContext sourceContext = new FetchSourceContext(true,
                        CollectionUtils.isNotEmpty(includeFieldSource) ? includeFieldSource.toArray(new String[includeFieldSource.size()]) : null,
                        CollectionUtils.isNotEmpty(excludeFieldSource) ? excludeFieldSource.toArray(new String[excludeFieldSource.size()]) : null);
                aggregationBuilder = AggregationBuilders.topHits(aggName)
                        .size(esField.size())
                        .fetchSource(sourceContext)
                        .sort(esFieldName, esField.sort());
                break;

        }
        return aggregationBuilder;
    }

    public static PipelineAggregationBuilder getPipelineAggregationBuilder(java.lang.reflect.Field aggField) {
        final AggregateField esField = aggField.getAnnotation(AggregateField.class);
        String aggName = aggField.getName();
        String esFieldName = esField.value();
        if (StringUtils.isBlank(esFieldName)) {
            esFieldName = aggName;
        }
        return new SumBucketPipelineAggregationBuilder(esFieldName, esField.bucketsPath());
    }


    @SneakyThrows
    public Aggregate summary(Query query) {
        SearchRequest searchRequest = new SearchRequest(getIndexOrDefault(null));
        final SearchSourceBuilder searchSourceBuilder = this.searchSourceBuilder(query);
        // 聚合
        if (!EmptyAggregate.class.equals(aggregateClass)) {
            final Collection<java.lang.reflect.Field> fields = this.getCacheSimpleFields(aggregateClass);
            for (java.lang.reflect.Field field : fields) {
                final Field docField = field.getAnnotation(Field.class);
                String aggName = field.getName();
                String esFieldName = docField.value();
                if (StringUtils.isBlank(esFieldName)) {
                    esFieldName = aggName;
                }

                final SumAggregationBuilder aggregationBuilder = AggregationBuilders.sum(aggName).field(esFieldName);
                searchSourceBuilder.aggregation(aggregationBuilder);
            }
        }
        searchRequest.source(searchSourceBuilder);
        // 结果
        SearchResponse response = elasticSearchClient.getClient().search(searchRequest, RequestOptions.DEFAULT);
        //聚合结果
        if (!EmptyAggregate.class.equals(aggregateClass)) {
            Aggregations aggregation = response.getAggregations();
            return this.buildAggResult(aggregation, aggregateClass);
        }
        return null;
    }


    @SneakyThrows
    public Pager<Document, Aggregate> query(Query query, boolean aggregate) {
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

        // 聚合
        if (!EmptyAggregate.class.equals(aggregateClass) && aggregate) {
            final Collection<java.lang.reflect.Field> fields = this.getCacheSimpleFields(aggregateClass);
            for (java.lang.reflect.Field field : fields) {
                final Field docField = field.getAnnotation(Field.class);
                String aggName = field.getName();
                String esFieldName = docField.value();
                if (StringUtils.isBlank(esFieldName)) {
                    esFieldName = aggName;
                }

                final SumAggregationBuilder aggregationBuilder = AggregationBuilders.sum(aggName).field(esFieldName);
                searchSourceBuilder.aggregation(aggregationBuilder);
            }
        }

        log.info("ESnativeQuery:{}", searchRequest.source().toString());

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
            this.emptyDateRecover(esPo);
        }

        // 封装
        Pager pageInfo = new Pager();
        pageInfo.setList(esPoList);
        pageInfo.setTotal(total);
        final Object aggregationResult = pageInfo.getAggregation();
        // 聚合结果
        if (!EmptyAggregate.class.equals(aggregateClass) && aggregate) {
            Aggregations aggregation = response.getAggregations();
            final Aggregate aggResult = this.buildAggResult(aggregation, aggregateClass);
            pageInfo.setAggregation(aggResult);
        }
        return pageInfo;
    }


    @SneakyThrows
    private Aggregate buildAggResult(Aggregations aggregation, Class<Aggregate> clazz) {
        if (aggregation == null) {
            return null;
        }
        final List<Aggregation> aggregations = aggregation.asList();
        final Map<String, Aggregation> aggregationAsMap = aggregation.getAsMap();
        final java.lang.reflect.Field[] fields = clazz.getDeclaredFields();

        final Aggregate aggResult = clazz.newInstance();
        for (java.lang.reflect.Field field : fields) {
            final Aggregation agg = aggregationAsMap.get(field.getName());
            if (Objects.nonNull(agg)
                    && agg instanceof ParsedSum
                    && BigDecimal.class.isAssignableFrom(field.getType())
            ) {
                ParsedSum ps = (ParsedSum) agg;
                final double value = ps.getValue();
                final BigDecimal bigDecimal = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
                field.setAccessible(true);
                try {
                    field.set(aggResult, bigDecimal);
                } catch (IllegalAccessException e) {
                    log.error("access exception", e);
                }
            }
        }
        return aggResult;
    }

    @SneakyThrows
    private <T> BoolQueryBuilder queryConditionAssembling(T query, boolean aggFilter) {
        BoolQueryBuilder boolQueryBuilderLast = QueryBuilders.boolQuery();
        final Collection<java.lang.reflect.Field> fields = this.getCacheSimpleFields(query.getClass());

        Map<String, RangeQueryBuilder> dateRangeMap = new HashMap<>();

        Map<String, RangeQueryBuilder> dateRangeMustNotMap = new HashMap<>();

        for (java.lang.reflect.Field field : fields) {
            final Field docField = field.getAnnotation(Field.class);
            String esFieldName = docField.value();
            if (StringUtils.isBlank(esFieldName)) {
                esFieldName = field.getName();
            }
            field.setAccessible(true);
            final Object fieldValue = field.get(query);
            final Class<?> fieldType = field.getType();

            if (Operation.OR.equals(docField.operation())) {
                final BoolQueryBuilder operationBuilder = this.dealOperation(fieldValue, docField.operation());
                boolQueryBuilderLast
                        .must(operationBuilder);
                continue;
            } else if (Operation.EXIST.equals(docField.operation())) {
                boolQueryBuilderLast
                        .must(QueryBuilders.existsQuery(esFieldName));
                continue;
            } else if (Operation.NOT_EXIST.equals(docField.operation())) {
                boolQueryBuilderLast
                        .mustNot(QueryBuilders.existsQuery(esFieldName));
                continue;
            }

            // 去除 值为null
            if (Objects.isNull(fieldValue)) {
                continue;
            }

            if (List.class.isAssignableFrom(fieldType)) {
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                List list = (List) fieldValue;
                // 去除 集合 size =0
                if (CollectionUtils.isEmpty(list)) {
                    continue;
                }

                ListOperation listOperation = docField.listOperation();
                if (listOperation != null && ListOperation.MUST_NOT.equals(listOperation)) {
                    mustnotShouldList(boolQueryBuilderLast, esFieldName, boolQueryBuilder, list);
                } else {
                    mustShouldList(boolQueryBuilderLast, esFieldName, boolQueryBuilder, list);
                }

            } else if (Date.class.isAssignableFrom(fieldType)) {
                long times = ((Date) fieldValue).getTime();
                RangeOperation rangeOperation = docField.rangeOperation();
                if (rangeOperation != null && RangeOperation.MUST_NOT.equals(rangeOperation)) {
                    buildRangeMap(dateRangeMustNotMap, docField, esFieldName, times);
                } else {
                    buildRangeMap(dateRangeMap, docField, esFieldName, times);
                }
            } else if (isBaseType(fieldType)) {
                String operationSymbols = docField.operationSymbols();
                if (String.class.isAssignableFrom(fieldType)) {
                    String val = (String) fieldValue;
                    // 去除 字符串 为""
                    if (StringUtils.isBlank(val)) {
                        continue;
                    }
                }
                if (esFieldName.indexOf("||") > 0) {
                    List<String> esFields = Splitter.on("||")
                            .trimResults()
                            .omitEmptyStrings()
                            .splitToList(esFieldName);
                    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                    for (String name : esFields) {
                        boolQueryBuilder.should(QueryBuilders.termQuery(name, fieldValue));
                    }
                    boolQueryBuilderLast.must(boolQueryBuilder);
                } else if (String.class.isAssignableFrom(fieldType) && StringUtils.isNotBlank(operationSymbols)) {
                    RangeOperation rangeOperation = docField.rangeOperation();
                    if (rangeOperation != null && RangeOperation.MUST_NOT.equals(rangeOperation)) {
                        buildRangeMap(dateRangeMustNotMap, docField, esFieldName, fieldValue);
                    } else {
                        buildRangeMap(dateRangeMap, docField, esFieldName, fieldValue);
                    }
                } else {
                    boolQueryBuilderLast
                            .must(QueryBuilders
                                    .termQuery(esFieldName, fieldValue));
                }

            } else {
                //   log.warn("不兼容的数据类型:{}", fieldType);
            }
        }

        //日期条件
        for (RangeQueryBuilder rangeQueryBuilder : dateRangeMap.values()) {
            boolQueryBuilderLast.must(rangeQueryBuilder);
        }

        for (RangeQueryBuilder rangeQueryBuilder : dateRangeMustNotMap.values()) {
            boolQueryBuilderLast.mustNot(rangeQueryBuilder);
        }

        if (!aggFilter) {
            // 过滤退改件 后期数据稳定后下线
            //boolQueryBuilderLast.must(new TermQueryBuilder("returnTag", 0));
            // 过滤老数据 后期数据稳定后下线
            final RangeQueryBuilder quryBuilder = this.getModifyTimeResetedQuryBuilder();
            if (quryBuilder != null) {
                boolQueryBuilderLast.must(quryBuilder);
            }
        }

        return boolQueryBuilderLast;
    }

    //对List生成must & Should
    private void mustShouldList(BoolQueryBuilder boolQueryBuilderLast, String esFieldName, BoolQueryBuilder boolQueryBuilder, List list) {
        for (int i = 0; i < list.size(); i++) {
            boolQueryBuilder
                    .should(QueryBuilders
                            .termQuery(esFieldName, list.get(i)));
        }
        boolQueryBuilderLast.must(boolQueryBuilder);
    }

    //对List生成must_not & Should
    private void mustnotShouldList(BoolQueryBuilder boolQueryBuilderLast, String esFieldName, BoolQueryBuilder boolQueryBuilder, List list) {
        for (int i = 0; i < list.size(); i++) {
            boolQueryBuilder
                    .should(QueryBuilders
                            .termQuery(esFieldName, list.get(i)));
        }
        boolQueryBuilderLast.mustNot(boolQueryBuilder);
    }

    private void buildRangeMap(Map<String, RangeQueryBuilder> dateRangeMap, Field docField, String esFieldName, Object times) {
        String operationSymbols = docField.operationSymbols();

        RangeQueryBuilder rangeBuilder;
        if (dateRangeMap.get(esFieldName) != null) {
            rangeBuilder = dateRangeMap.get(esFieldName);
        } else {
            rangeBuilder = QueryBuilders.rangeQuery(esFieldName);
        }
        if (">".equals(operationSymbols)) {
            rangeBuilder.gt(times);
        } else if (">=".equals(operationSymbols)) {
            rangeBuilder.gte(times);
        } else if ("<".equals(operationSymbols)) {
            rangeBuilder.lt(times);
        } else if ("<=".equals(operationSymbols)) {
            rangeBuilder.lte(times);
        } else {
            log.warn("不兼容的操作符号:{}", operationSymbols);
        }

        dateRangeMap.put(esFieldName, rangeBuilder);
    }

    /**
     * 处理 多字段 或 查询
     *
     * @param entity
     * @param operation
     * @return
     */
    @SneakyThrows
    private BoolQueryBuilder dealOperation(final Object entity, Operation operation) {
        BoolQueryBuilder boolQueryBuilderLast = QueryBuilders.boolQuery();
        if (!Operation.OR.equals(operation)) {
            return boolQueryBuilderLast;
        }
        final Class<?> aClass = entity.getClass();

        final Collection<java.lang.reflect.Field> fields = this.getCacheSimpleFields(aClass);
        for (java.lang.reflect.Field field : fields) {
            final Field docField = field.getAnnotation(Field.class);
            String esFieldName = docField.value();
            if (StringUtils.isBlank(esFieldName)) {
                esFieldName = field.getName();
            }
            field.setAccessible(true);
            final Object fieldValue = field.get(entity);
            final Class<?> fieldType = field.getType();
            // 去除 值为null
            if (Objects.isNull(fieldValue)) {
                continue;
            }
            if (List.class.isAssignableFrom(fieldType)) {
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                List list = (List) fieldValue;
                // 去除 集合 size =0
                if (CollectionUtils.isEmpty(list)) {
                    continue;
                }
                for (int i = 0; i < list.size(); i++) {
                    boolQueryBuilder
                            .should(QueryBuilders
                                    .termQuery(esFieldName, list.get(i)));
                }
                boolQueryBuilderLast.should(boolQueryBuilder);
            } else if (isBaseType(fieldType)) {
                if (String.class.isAssignableFrom(fieldType)) {
                    String val = (String) fieldValue;
                    // 去除 字符串 为""
                    if (StringUtils.isBlank(val)) {
                        continue;
                    }
                }
                if (esFieldName.indexOf("||") > 0) {
                    List<String> esFields = Splitter.on("||")
                            .trimResults()
                            .omitEmptyStrings()
                            .splitToList(esFieldName);
                    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                    for (String name : esFields) {
                        boolQueryBuilder.should(QueryBuilders.termQuery(name, fieldValue));
                    }
                    boolQueryBuilderLast.should(boolQueryBuilder);
                } else {
                    boolQueryBuilderLast
                            .should(QueryBuilders
                                    .termQuery(esFieldName, fieldValue));
                }

            } else {
                //  log.warn("不兼容的数据类型:{}", fieldType);
            }
        }

        return boolQueryBuilderLast;
    }

    /**
     * @Description: 位操作
     */
    @SneakyThrows
    private BoolQueryBuilder dealBitOperation(final Object entity, Operation operation) {
        BoolQueryBuilder boolQueryBuilderLast = QueryBuilders.boolQuery();
//        boolQueryBuilderLast.must(QueryBuilders.scriptQuery(new Script("(doc['id'].value >> 3)==3")));
        return boolQueryBuilderLast;
    }


    public abstract void sort(Query query, SearchSourceBuilder builder);


    public DeepResponse<Document> deepOne(Query query) {
        String scrollId = query.getScrollId();
        if (StringUtils.isEmpty(scrollId)) {
            return this.getPartitionDeepForNoScrollId(query);
        }
        return this.getPartitionDeepForHasScrollId(query);
    }

    public List<Document> deepQuery(Query query) {
        List<Document> list = Lists.newArrayList();
        DeepResponse<Document> deepResponse = this.deepOne(query);
        while (deepResponse.getDataList().size() > 0) {
            list.addAll(deepResponse.getDataList());
            query.setScrollId(deepResponse.getScrollId());
            deepResponse = this.deepOne(query);
        }
        return list;
    }

    @Override
    public List<Document> autoQuery(Query query) {
        final long count = this.count(query);
        List<Document> list = Lists.newArrayList();
        if (count < INDEX_MAX_RESULT_WINDOW) {
            query.setPageNum(1);
            query.setPageSize(INDEX_MAX_RESULT_WINDOW);
            Pager<Document, Aggregate> pageInfo = this.query(query, false);
            list = pageInfo.getList();
        } else {
            list = this.deepQuery(query);
        }
        return list;
    }

    /**
     * 时间转换
     *
     * @param date
     * @return
     */
    private Object transferTime(Object date) {
        if (date == null) {
            return date;
        }

        if (date instanceof Date) {
            Date d = (Date) date;
            return d.getTime();
        }
        // Long / String
        return date;
    }

    /**
     * 分页滚动查询 无 scrollId
     *
     * @return
     * @throws IOException
     */
    @SneakyThrows
    private DeepResponse<Document> getPartitionDeepForNoScrollId(Query query) {
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
        this.sort(query, searchSourceBuilder);

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
            this.emptyDateRecover(esPo);
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
    private DeepResponse<Document> getPartitionDeepForHasScrollId(Query query) {
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

    protected SearchSourceBuilder searchSourceBuilder(Query query) {
        BoolQueryBuilder boolQueryBuilderLast = this.queryConditionAssembling(query, false);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.trackTotalHits(true);
        searchSourceBuilder.query(boolQueryBuilderLast);
        return searchSourceBuilder;
    }

    final HashSet<Class<? extends Serializable>> classes = Sets.newHashSet(String.class, Number.class, Boolean.class, Character.class);

    private boolean isBaseType(Class clazz) {
        return classes.stream().anyMatch(item -> item.isAssignableFrom(clazz));
    }

    @Override
    public List<Aggregate> summaryCustom(Query query) {
        return null;
    }

    @SneakyThrows
    public <T> List<T> summaryTerm(Query query, Class<T> aggClazz) {
        SearchRequest searchRequest = new SearchRequest(getIndexOrDefault(null));
        final SearchSourceBuilder searchSourceBuilder = this.searchSourceBuilder(query);
        searchSourceBuilder.size(0);
        // 聚合
        if (aggClazz != null) {
            final Collection<java.lang.reflect.Field> fields = getCacheSimpleFields(aggClazz);
            final Pair<List<AggregationBuilder>, List<PipelineAggregationBuilder>> aggregate = aggregate(aggClazz, query);
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
            data = this.dealTermAgg(response.getAggregations(), aggClazz);
        }
        return data;
    }

    @SneakyThrows
    public List<Aggregate> summaryTerm(Query query) {
        SearchRequest searchRequest = new SearchRequest(getIndexOrDefault(null));
        final SearchSourceBuilder searchSourceBuilder = this.searchSourceBuilder(query);
        // 聚合
        if (!EmptyAggregate.class.equals(aggregateClass)) {
            final Collection<java.lang.reflect.Field> fields = getCacheSimpleFields(aggregateClass);
            final Pair<List<AggregationBuilder>, List<PipelineAggregationBuilder>> aggregate = aggregate(aggregateClass, query);
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
        List<Aggregate> data = Lists.newArrayList();
        if (!EmptyAggregate.class.equals(aggregateClass)) {
            data = this.dealTermAgg(response.getAggregations(), aggregateClass);
        }
        return data;
    }

    private List dealTerm(Aggregation agg, Class agClazz, Object parent) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
        String fieldName = agg.getName();
        ParsedTerms item = (ParsedTerms) agg;

        final List<? extends Terms.Bucket> buckets = item.getBuckets();
        final List<Object> subEntitys = Lists.newArrayList();
        for (Terms.Bucket bucket : buckets) {
            final Object termObj = agClazz.newInstance();
            ReflectionUtil.setFieldValue(termObj, fieldName, bucket.getKeyAsString());
            subEntitys.add(termObj);
            final Aggregations aggregations = bucket.getAggregations();
            this.parseAggs(bucket.getAggregations(), agClazz, termObj);

        }
        return subEntitys;

    }

    /**
     * 存在分组   List
     * 无分组聚合 Object
     *
     * @param aggregations
     * @param agClazz
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @SneakyThrows
    protected List dealTermAgg(Aggregations aggregations, Class agClazz) throws IllegalAccessException, InstantiationException {
        Object instance = null;
        for (Aggregation agg : aggregations) {
            String fieldName = agg.getName();
            if (agg instanceof ParsedTerms) {
                return this.dealTerm(agg, agClazz, null);
            } else {
                if (instance == null) {
                    instance = agClazz.newInstance();
                }
                this.reflectSet(agg, instance, fieldName);
            }
        }
        return Lists.newArrayList(instance);
    }

    private List dealFilter(Aggregation agg, Class agClazz, Object parent) throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        String fieldName = agg.getName();
        ParsedFilter item = (ParsedFilter) agg;
        final Aggregations aggregations = item.getAggregations();

        final List<Object> subEntitys = Lists.newArrayList();

        final Object termObj = agClazz.newInstance();
        subEntitys.add(termObj);

        this.parseAggs(item.getAggregations(), agClazz, termObj);

        return subEntitys;
    }

    private void parseAggs(Aggregations aggregations, Class agClazz, Object termObj) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        for (Aggregation aggregation : aggregations) {
            String subAggName = aggregation.getName();
            if (aggregation instanceof ParsedTerms) {
                final Optional<java.lang.reflect.Field> subTermField = this.getSubTermField(agClazz, subAggName);
                if (subTermField.isPresent()) {
                    final java.lang.reflect.Field subField = subTermField.get();
                    final String subTermFieldName = subField.getName();
                    final Class subClass = (Class) ((ParameterizedType) subField.getGenericType()).getActualTypeArguments()[0];
                    final List list = this.dealTerm(aggregation, subClass, termObj);
                    ReflectionUtil.setFieldValue(termObj, subTermFieldName, list);
                }

            } else if (aggregation instanceof ParsedFilter) {
                final Optional<java.lang.reflect.Field> subTermField = this.getSubTermField(agClazz, subAggName);
                if (subTermField.isPresent()) {
                    final java.lang.reflect.Field subField = subTermField.get();
                    final String subTermFieldName = subField.getName();
                    final Class subClass = (Class) ((ParameterizedType) subField.getGenericType()).getActualTypeArguments()[0];
                    final List list = this.dealFilter(aggregation, subClass, termObj);
                    ReflectionUtil.setFieldValue(termObj, subTermFieldName, list);
                }
            } else {
                this.reflectSet(aggregation, termObj, subAggName);
            }
        }
    }

    private void reflectSet(Aggregation agg, Object parent, String fieldName) {
        if (agg instanceof ParsedValueCount) {
            ReflectionUtil.setFieldValue(parent, fieldName, ((ParsedValueCount) agg).getValue());
        } else if (agg instanceof ParsedSum) {
            ParsedSum aggsValue = (ParsedSum) agg;
            final BigDecimal value = BigDecimal.valueOf(aggsValue.getValue());
            ReflectionUtil.setFieldValue(parent, fieldName, value);
        } else if (agg instanceof ParsedCardinality) {
            ParsedCardinality aggsValue = (ParsedCardinality) agg;
            ReflectionUtil.setFieldValue(parent, fieldName, aggsValue.getValue());
        } else if (agg instanceof ParsedSimpleValue) {
            ParsedSimpleValue aggsValue = (ParsedSimpleValue) agg;
            ReflectionUtil.setFieldValue(parent, fieldName, BigDecimal.valueOf(aggsValue.value()));
        } else if (agg instanceof ParsedTopHits) {
            ParsedTopHits aggsValus = (ParsedTopHits) agg;
            final SearchHit[] hits = aggsValus.getHits().getHits();
            List<Document> esPoList = new ArrayList<>();
            for (SearchHit searchHit : hits) {
                String json = searchHit.getSourceAsString();
                esPoList.add(JsonUtil.parse(json, documentClass));
            }
            ReflectionUtil.setFieldValue(parent, fieldName, esPoList);
        }
    }


    private Optional<java.lang.reflect.Field> getTermField(Class clazz) {
        final java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        final Optional<java.lang.reflect.Field> first = Arrays.stream(fields).filter(f -> f.isAnnotationPresent(TermField.class)).findFirst();
        return first;
    }

    private Optional<java.lang.reflect.Field> getSubTermField(Class clazz, String name) {
        final java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        final Optional<java.lang.reflect.Field> first = Arrays.stream(fields)
                .filter(f -> f.isAnnotationPresent(AggregateField.class))
                .filter(f -> f.getAnnotation(AggregateField.class).aggType().equals(AggregateField.AggType.SUB_AGG))
                .filter(f -> !StringUtils.isNotBlank(name) || f.getAnnotation(AggregateField.class).value().equals(name))
                .findFirst();
        return first;
    }


    /**
     * 根据修改时间过滤
     *
     * @return
     */
    public RangeQueryBuilder getModifyTimeResetedQuryBuilder() {
        // 获取配置天数 且 配置天数须 >=0
        Integer days = esSearchLatestDays.get(GLOBAL_LATEST_DAYS);
        if (days == null || days < 0) {
            return null;
        }
        // 设置时间
        final LocalDateTime startLocalDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).minusDays(days);
        Date startDate = Date.from(startLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
        final RangeQueryBuilder rangeQueryBuilder = new RangeQueryBuilder("modifyTime");
        rangeQueryBuilder.gte(startDate.getTime());
        return rangeQueryBuilder;
    }
}