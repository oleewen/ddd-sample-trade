package com.transformer.es.service;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.transformer.es.service.annotation.AggregateField;
import com.transformer.es.service.annotation.TermField;
import com.transformer.es.service.model.*;
import com.transformer.helper.JsonHelper;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.*;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedCardinality;
import org.elasticsearch.search.aggregations.metrics.ParsedSum;
import org.elasticsearch.search.aggregations.metrics.ParsedTopHits;
import org.elasticsearch.search.aggregations.metrics.ParsedValueCount;
import org.elasticsearch.search.aggregations.pipeline.ParsedSimpleValue;
import org.elasticsearch.search.aggregations.pipeline.SumBucketPipelineAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.core.ResolvableType;
import org.springside.modules.utils.reflect.ReflectionUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 描述：ES 查询辅助
 * 作者：董兵
 * 时间：2023/8/24 14:26
 */
public class EsServiceUtils {
    /**
     * 默认空时间
     */
    public static final String DEFAULT_EMPTY_DATE = "1900-01-01 00:00:00";
    /**
     * 默认查询结果集大小
     */
    private static final int INDEX_MAX_RESULT_WINDOW = 10000;

    /**
     * 查询时间范围 根据Apollo配置调整
     */
    public static final String GLOBAL_LATEST_DAYS = "_global";

    protected static Multimap<Class, Field> dateFieldCache = ArrayListMultimap.create();
    protected static ConcurrentHashMap<Class, List<Field>> simpleFieldCache = new ConcurrentHashMap();
    /**
     * 基本数据类型
     */
    final static HashSet<Class<? extends Serializable>> BASIC_CLASSES = Sets.newHashSet(String.class, Number.class, Boolean.class, Character.class);


    @SneakyThrows
    public static <T> void emptyDateInit(T document) {
        final Collection<Field> cacheDateFields = getCacheDateFields(document.getClass());
        if (CollectionUtils.isEmpty(cacheDateFields)) {
            return;
        }
        for (Field field : cacheDateFields) {
            field.setAccessible(true);
            final String o = (String) field.get(document);
            if (StringUtils.isBlank(o)) {
                field.set(document, DEFAULT_EMPTY_DATE);
            }
        }
    }

    @SneakyThrows
    public static <T> void emptyDateRecover(T document) {
        final Collection<Field> cacheDateFields = getCacheDateFields(document.getClass());
        if (CollectionUtils.isEmpty(cacheDateFields)) {
            return;
        }
        for (Field field : cacheDateFields) {
            field.setAccessible(true);
            final String o = (String) field.get(document);
            if (DEFAULT_EMPTY_DATE.equals(o)) {
                field.set(document, "");
            }
        }
    }

    public static Collection<Field> getCacheSimpleFields(Class<?> clazz) {
        final Collection<Field> fieldCollection = simpleFieldCache.get(clazz);
        if (CollectionUtils.isNotEmpty(fieldCollection)) {
            return fieldCollection;
        }
        final List<Field> data = EsServiceUtils.getFieldList(clazz).stream().filter(f -> f.isAnnotationPresent(com.transformer.es.service.annotation.Field.class)).collect(Collectors.toList());
        simpleFieldCache.put(clazz, data);
        return simpleFieldCache.get(clazz);
    }

    private static List<Field> getFieldList(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null && !clazz.equals(Object.class)) {
            final Field[] fields = clazz.getDeclaredFields();
            fieldList.addAll(Arrays.asList(fields));
            clazz = clazz.getSuperclass();
        }
        return fieldList;
    }

    private static Collection<Field> getCacheDateFields(Class<?> clazz) {
        final Collection<Field> fieldCollection = dateFieldCache.get(clazz);
        if (CollectionUtils.isNotEmpty(fieldCollection)) {
            return fieldCollection;
        }

        final Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            final boolean present = field.isAnnotationPresent(com.transformer.es.service.annotation.Field.class);
            if (!present) {
                continue;
            }
            final com.transformer.es.service.annotation.Field docField = field.getAnnotation(com.transformer.es.service.annotation.Field.class);
            if (docField.type().equals(FieldType.DATE)) {
                fieldCollection.add(field);
            }
        }

        return dateFieldCache.get(clazz);
    }

    @SneakyThrows
    public static <T> Pair<List<AggregationBuilder>, List<PipelineAggregationBuilder>> aggregate(Class clazz, T query) {
        final ArrayList<AggregationBuilder> aggBuilders = Lists.newArrayList();
        final ArrayList<PipelineAggregationBuilder> aggPipeBuilders = Lists.newArrayList();


        final Field[] fields = clazz.getDeclaredFields();
        final List<Field> fieldList = Arrays.asList(fields);
        // 聚合字段
        List<Field> aggFields = fieldList.stream().filter(f -> f.isAnnotationPresent(AggregateField.class)).collect(Collectors.toList());

        // 处理分组
        final Optional<Field> term = fieldList.stream()
                .filter(f -> f.isAnnotationPresent(TermField.class))
                .findFirst();

        AggregationBuilder aggBuilder = null;
        if (term.isPresent()) {
            final Field field = term.get();
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
        final List<Field> subAggFields = aggFields.stream()
                .filter(f -> f.getAnnotation(AggregateField.class).aggType().equals(AggregateField.AggType.SUB_AGG))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(subAggFields)) {
            final Field[] queryFields = query.getClass().getDeclaredFields();
            for (Field subAggField : subAggFields) {

                final AggregateField annotation = subAggField.getAnnotation(AggregateField.class);
                Class filterClazz = annotation.filter();
                FilterAggregationBuilder filter = null;
                if (filterClazz != null && !filterClazz.equals(EmptyFilter.class)) {
                    // query 赋值 filter 中
                    Object instance = filterClazz.newInstance();
                    final Field[] declaredFields = filterClazz.getDeclaredFields();
                    for (Field declaredField : declaredFields) {
                        final String fieldName = declaredField.getName();
                        for (Field queryField : queryFields) {
                            if (StringUtils.equals(fieldName, queryField.getName())) {
                                final Object value = ReflectionUtil.getFieldValue(instance, fieldName);
                                ReflectionUtil.setFieldValue(instance, declaredField.getName(), value);
                            }
                        }
                    }
                    filter = AggregationBuilders.filter(annotation.value(), EsServiceUtils.queryConditionAssembling(instance, true));
                    aggBuilder.subAggregation(filter);
                }

                final ResolvableType type = ResolvableType.forField(subAggField);
                final Class subAggClazz = (Class) type.getGeneric(0).getType();
                final Pair<List<AggregationBuilder>, List<PipelineAggregationBuilder>> listPair = EsServiceUtils.aggregate(subAggClazz, query);
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
        for (Field aggField : aggFields) {
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

    private static AggregationBuilder getAggregationBuilder(Field aggField) {
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

    public static PipelineAggregationBuilder getPipelineAggregationBuilder(Field aggField) {
        final AggregateField esField = aggField.getAnnotation(AggregateField.class);
        String aggName = aggField.getName();
        String esFieldName = esField.value();
        if (StringUtils.isBlank(esFieldName)) {
            esFieldName = aggName;
        }
        return new SumBucketPipelineAggregationBuilder(esFieldName, esField.bucketsPath());
    }


    @SneakyThrows
    private static <T> T buildAggResult(Aggregations aggregation, Class<T> clazz) {
        if (aggregation == null) {
            return null;
        }
        final List<Aggregation> aggregations = aggregation.asList();
        final Map<String, Aggregation> aggregationAsMap = aggregation.getAsMap();
        final Field[] fields = clazz.getDeclaredFields();

        final T aggResult = clazz.newInstance();
        for (Field field : fields) {
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
                    //log.error("access exception", e);
                }
            }
        }
        return aggResult;
    }

    @SneakyThrows
    public static <T> BoolQueryBuilder queryConditionAssembling(T query, boolean aggFilter) {
        BoolQueryBuilder boolQueryBuilderLast = QueryBuilders.boolQuery();
        final Collection<Field> fields = EsServiceUtils.getCacheSimpleFields(query.getClass());

        Map<String, RangeQueryBuilder> dateRangeMap = new HashMap<>();

        Map<String, RangeQueryBuilder> dateRangeMustNotMap = new HashMap<>();

        for (Field field : fields) {
            final com.transformer.es.service.annotation.Field docField = field.getAnnotation(com.transformer.es.service.annotation.Field.class);
            String esFieldName = docField.value();
            if (StringUtils.isBlank(esFieldName)) {
                esFieldName = field.getName();
            }
            field.setAccessible(true);
            final Object fieldValue = field.get(query);
            final Class<?> fieldType = field.getType();

            if (Operation.OR.equals(docField.operation())) {
                final BoolQueryBuilder operationBuilder = EsServiceUtils.dealOperation(fieldValue, docField.operation());
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

        return boolQueryBuilderLast;
    }

    //对List生成must & Should
    private static void mustShouldList(BoolQueryBuilder boolQueryBuilderLast, String esFieldName, BoolQueryBuilder boolQueryBuilder, List list) {
        for (int i = 0; i < list.size(); i++) {
            boolQueryBuilder
                    .should(QueryBuilders
                            .termQuery(esFieldName, list.get(i)));
        }
        boolQueryBuilderLast.must(boolQueryBuilder);
    }

    //对List生成must_not & Should
    private static void mustnotShouldList(BoolQueryBuilder boolQueryBuilderLast, String esFieldName, BoolQueryBuilder boolQueryBuilder, List list) {
        for (int i = 0; i < list.size(); i++) {
            boolQueryBuilder
                    .should(QueryBuilders
                            .termQuery(esFieldName, list.get(i)));
        }
        boolQueryBuilderLast.mustNot(boolQueryBuilder);
    }

    private static void buildRangeMap(Map<String, RangeQueryBuilder> dateRangeMap, com.transformer.es.service.annotation.Field docField, String esFieldName, Object times) {
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
            //log.warn("不兼容的操作符号:{}", operationSymbols);
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
    private static BoolQueryBuilder dealOperation(final Object entity, Operation operation) {
        BoolQueryBuilder boolQueryBuilderLast = QueryBuilders.boolQuery();
        if (!Operation.OR.equals(operation)) {
            return boolQueryBuilderLast;
        }
        final Class<?> aClass = entity.getClass();

        final Collection<Field> fields = EsServiceUtils.getCacheSimpleFields(aClass);
        for (Field field : fields) {
            final com.transformer.es.service.annotation.Field docField = field.getAnnotation(com.transformer.es.service.annotation.Field.class);
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


    private static boolean isBaseType(Class clazz) {
        return BASIC_CLASSES.stream().anyMatch(item -> item.isAssignableFrom(clazz));
    }


    private static <T, R> List<R> dealTerm(Aggregation agg, Class<R> agClazz, Object parent, Class<T> docClazz) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
        String fieldName = agg.getName();
        ParsedTerms item = (ParsedTerms) agg;

        final List<? extends Terms.Bucket> buckets = item.getBuckets();
        final List<R> subEntitys = Lists.newArrayList();
        for (Terms.Bucket bucket : buckets) {
            final R termObj = agClazz.newInstance();
            ReflectionUtil.setFieldValue(termObj, fieldName, bucket.getKeyAsString());
            subEntitys.add(termObj);
            final Aggregations aggregations = bucket.getAggregations();
            EsServiceUtils.parseAggs(bucket.getAggregations(), agClazz, termObj, docClazz);

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
    public static <T, R> List<R> dealTermAgg(Aggregations aggregations, Class<R> agClazz, Class<T> docClazz) throws IllegalAccessException, InstantiationException {
        R instance = null;
        for (Aggregation agg : aggregations) {
            String fieldName = agg.getName();
            if (agg instanceof ParsedTerms) {
                return EsServiceUtils.dealTerm(agg, agClazz, null, docClazz);
            } else {
                if (instance == null) {
                    instance = agClazz.newInstance();
                }
                EsServiceUtils.reflectSet(agg, instance, fieldName, docClazz);
            }
        }
        return Lists.newArrayList(instance);
    }

    private static <R, T> List<R> dealFilter(Aggregation agg, Class<R> agClazz, Object parent, Class<T> docClazz) throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        String fieldName = agg.getName();
        ParsedFilter item = (ParsedFilter) agg;
        final Aggregations aggregations = item.getAggregations();

        final List<R> subEntitys = Lists.newArrayList();

        final R termObj = agClazz.newInstance();
        subEntitys.add(termObj);

        EsServiceUtils.parseAggs(item.getAggregations(), agClazz, termObj, docClazz);

        return subEntitys;
    }

    private static <T, R> void parseAggs(Aggregations aggregations, Class<R> agClazz, Object termObj, Class<T> docClazz) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        for (Aggregation aggregation : aggregations) {
            String subAggName = aggregation.getName();
            if (aggregation instanceof ParsedTerms) {
                final Optional<Field> subTermField = EsServiceUtils.getSubTermField(agClazz, subAggName);
                if (subTermField.isPresent()) {
                    final Field subField = subTermField.get();
                    final String subTermFieldName = subField.getName();
                    final Class subClass = (Class) ((ParameterizedType) subField.getGenericType()).getActualTypeArguments()[0];
                    final List list = EsServiceUtils.dealTerm(aggregation, subClass, termObj, docClazz);
                    ReflectionUtil.setFieldValue(termObj, subTermFieldName, list);
                }

            } else if (aggregation instanceof ParsedFilter) {
                final Optional<Field> subTermField = EsServiceUtils.getSubTermField(agClazz, subAggName);
                if (subTermField.isPresent()) {
                    final Field subField = subTermField.get();
                    final String subTermFieldName = subField.getName();
                    final Class subClass = (Class) ((ParameterizedType) subField.getGenericType()).getActualTypeArguments()[0];
                    final List list = EsServiceUtils.dealFilter(aggregation, subClass, termObj, docClazz);
                    ReflectionUtil.setFieldValue(termObj, subTermFieldName, list);
                }
            } else {
                EsServiceUtils.reflectSet(aggregation, termObj, subAggName, docClazz);
            }
        }
    }

    public static <T> void reflectSet(Aggregation agg, Object parent, String fieldName, Class<T> documentClazz) {
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
            List<T> esPoList = new ArrayList<>();
            for (SearchHit searchHit : hits) {
                String json = searchHit.getSourceAsString();
                esPoList.add(JsonHelper.parseObject(json, documentClazz));
            }
            ReflectionUtil.setFieldValue(parent, fieldName, esPoList);
        }
    }


    private static Optional<Field> getTermField(Class clazz) {
        final Field[] fields = clazz.getDeclaredFields();
        final Optional<Field> first = Arrays.stream(fields).filter(f -> f.isAnnotationPresent(TermField.class)).findFirst();
        return first;
    }

    private static Optional<Field> getSubTermField(Class clazz, String name) {
        final Field[] fields = clazz.getDeclaredFields();
        final Optional<Field> first = Arrays.stream(fields)
                .filter(f -> f.isAnnotationPresent(AggregateField.class))
                .filter(f -> f.getAnnotation(AggregateField.class).aggType().equals(AggregateField.AggType.SUB_AGG))
                .filter(f -> !StringUtils.isNotBlank(name) || f.getAnnotation(AggregateField.class).value().equals(name))
                .findFirst();
        return first;
    }


    /**
     * 修改时间获取
     *
     * @param esSearchLatestDays
     * @return
     */
    public static RangeQueryBuilder getModifyTimeResetedQuryBuilder(HashMap<String, Integer> esSearchLatestDays) {
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
