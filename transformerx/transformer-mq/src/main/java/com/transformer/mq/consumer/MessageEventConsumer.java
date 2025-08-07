package com.transformer.mq.consumer;

import com.transformer.mq.event.EmptyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import javax.annotation.Resource;

/**
 * @author ouliyuan 2023/7/18
 */
@Slf4j
public abstract class MessageEventConsumer<T> extends MessageConsumer<T> {
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    protected boolean accept(T message) {
        // 将消息封装为事件
        ApplicationEvent event = buildEvent(message);
        if (event == null) {
            log.warn("message parse to event is null:{}", message);
            return false;
        }

        // 空事件，直接返回成功
        if(event instanceof EmptyEvent){
            return true;
        }

        // 发布业务事件
        applicationEventPublisher.publishEvent(event);
        return true;
    }

    protected abstract ApplicationEvent buildEvent(T message);
}
