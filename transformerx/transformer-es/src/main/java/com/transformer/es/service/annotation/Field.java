package com.transformer.es.service.annotation;



import com.transformer.es.service.model.FieldType;
import com.transformer.es.service.model.ListOperation;
import com.transformer.es.service.model.Operation;
import com.transformer.es.service.model.RangeOperation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述：Es 字段
 * 作者：董兵
 * 时间：2021/5/10 15:30
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Field {
    String value() default "";

    FieldType type() default FieldType.NO;

    String format() default "";

    int scaling_factor() default 100;

    Operation operation() default Operation.NOT;

    ListOperation listOperation() default ListOperation.MUST;

    RangeOperation rangeOperation() default RangeOperation.MUST;

    String operationSymbols() default "";

}
