package com.transformer.es.service.annotation;

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
public @interface TermField {
    String value() default "";

    // 当 aggType类型为 term时, 启用
    int size();

}

