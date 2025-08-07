package com.transformer.es.service.annotation;

import com.transformer.es.service.model.EmptyFilter;
import com.transformer.es.service.model.FieldType;
import org.elasticsearch.search.sort.SortOrder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述：ES 聚合字段
 * 作者：董兵
 * 时间：2021/7/5 10:18
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AggregateField {
    String value() default "";

    FieldType type() default FieldType.NO;

    AggType aggType();

    int order() default 0;

    String bucketsPath() default "";

    SortOrder sort() default SortOrder.DESC; //desc ||aes

    /**
     * top_hit 使用
     * @return
     */
    int size() default 1;
    String includeSources() default "";
    String excludeSources() default "";

    /**
     * 聚合过滤器
     * @return
     */
    Class filter() default EmptyFilter.class;

    enum AggType {
        TERM,// 数据类型必须为 String
        SUM,
        AVG,
        COUNT,
        MIN,
        MAX,
        SUB_AGG, // 数据类型必须为 List
        CARDINALITY,
        SUM_BUCKETS,
        TOP_HITS,
    }

}

