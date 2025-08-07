package com.transformer.mq.event;

import com.transformer.mq.message.Message;
import org.springframework.context.ApplicationEvent;

/**
 * @author ouliyuan 2023/7/17
 */
public class EmptyEvent extends ApplicationEvent {
    public EmptyEvent(Message message) {
        super(message);
    }
}
