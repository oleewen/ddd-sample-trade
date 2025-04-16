package com.transformer.event;

import com.transformer.result.ResultSupport;
import lombok.Getter;

/**
 * 抽象result
 *
 * @author only
 * Date 2015/8/19.
 */
public abstract class AbstractResult<T> implements ResultSupport {
    @Getter
    private T module;
}
