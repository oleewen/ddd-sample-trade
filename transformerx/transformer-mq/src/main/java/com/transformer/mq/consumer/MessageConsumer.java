package com.transformer.mq.consumer;

import lombok.extern.slf4j.Slf4j;

/**
 * 消息通用监听器
 *
 * @author ouliyuan 2023/06/27
 */
@Slf4j
public abstract class MessageConsumer<T> {

    protected boolean handler(String messageBody) {
        // 消息体转消息对象
        T message;
        try {
            message = getMessage(messageBody);
        } catch (Exception e) {
            throw new IllegalArgumentException("message parse to object exception", e);
        }

        if (message == null) {
            log.warn("message parse to object is null:{}", messageBody);
            return false;
        }

        return accept(message);
    }

    protected abstract T getMessage(String message);

    protected abstract boolean accept(T message);

}
