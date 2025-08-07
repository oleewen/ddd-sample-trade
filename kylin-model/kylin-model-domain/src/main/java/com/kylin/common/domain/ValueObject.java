package com.kylin.common.domain;

/**
 * 值对象
 *
 * @author only
 * @since 2020-05-22
 */
public interface ValueObject<T> {
    /**
     * 取值对象的值
     *
     * @return 值对象值
     */
    T value();
}
