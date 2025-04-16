package com.transformer.event.listener;

import com.transformer.event.AbstractEvent;
import com.transformer.event.AbstractResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author only
 * @Date 2015/8/18.
 */
public abstract class AbstractEventListener<E extends AbstractEvent, R extends AbstractResult> implements java.util.EventListener {

    /** 日志对象 */
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public void handleEvent(E event) {
        long start = System.currentTimeMillis();
        try {
            R result = null;
            if (onCheck(event)) {
                fillIfNeed(event);
                result = onHandle(event);
            }
            onComplete(event, result);
        } catch (Exception e) {
            onException(event, e);
        } finally {
            long end = System.currentTimeMillis();

            logger.warn("handleEvent\01event\02{}\01ms\02{}", event.getClass().getSimpleName(), (end - start));
        }
    }

    protected boolean onCheck(E event) {
        if (!event.hasModule()) {
            event.onFailure("event msg is required");
        }

        return true;
    }

    protected void fillIfNeed(E event) {
    }

    protected abstract R onHandle(E event);

    protected void onComplete(E event, R result) {
        if (result != null && result.isSuccess()) {
            event.onSuccess();
            return;
        }

        event.onFailure(null);
    }

    protected void onException(E event, Exception e) {
        event.onException(e);
    }
}
