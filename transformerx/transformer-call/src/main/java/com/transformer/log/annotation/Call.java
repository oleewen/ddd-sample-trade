package com.transformer.log.annotation;

import java.lang.annotation.*;

/**
 * 日志注解
 *
 * @author only
 * @date 2017-08-16
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Call {
    /** 日志名 */
    String value() default "";

    /** 耗时阀值 */
    int elapsed() default 1000;

    /** 采样机率:sample/10000 */
    int sample() default 1;

    /** 采样基数：默认10000 */
    int basic() default 10000;

    /** 状态码 */
    int status() default 0;

    /** 错误码 */
    String errorCode() default "";

    /** 错误信息 */
    String errorMessage() default "";
}
