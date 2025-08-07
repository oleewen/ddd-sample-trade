package com.transformer.log.annotation;

import java.lang.annotation.*;

/**
 * 运行诊断注解
 * User: only
 * Date: 14-6-20
 * Time: 下午5:40
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface Profiler {
	/** 诊断名称：默认用方法签名 */
	String value() default "";

	/** 耗时阀值 */
	int elapsed() default 1000;

	/** 采样机率:sample/10000 */
	int sample() default 1;

	/** 采样基数：默认10000 */
	int basic() default 10000;
}
