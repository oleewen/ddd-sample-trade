package com.transformer.event;

import com.google.common.base.Throwables;
import com.transformer.exception.helper.ExceptionHelper;
import com.transformer.helper.JsonHelper;
import com.transformer.context.Context;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author only
 *         Date 2015/8/18.
 */
public abstract class AbstractEvent<T> implements Event {
    protected T module;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final EventFuture future = new EventFuture();

    protected AbstractEvent(T module) {
        this.module = module;
    }

    public boolean waitDone(long timeout) {
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS) != null;
        } catch (Exception e) {
            logger.error(String.format("waitDone\01event\02%s\01timeout\02%s\01exception\02%s", JsonHelper.toJson(this), TimeUnit.MILLISECONDS.toMillis(timeout), Throwables.getRootCause(e)));

            throw ExceptionHelper.createNestedException(e);
        }
    }

    public void onSuccess() {
        String message = "event complete successful";

        future.setSuccess(message);

        if (Context.debug() || logger.isInfoEnabled()) {
            logger("success", message);
        }
    }

    public void onFailure(String message) {
        message = StringUtils.isNotBlank(message) ? message : "event complete failure";

        future.setFailure(message);

        logger("failure", message);
    }

    private void logger(String status, String message) {
        logger.warn("handleEvent:{}, event:{}, message:{}", status, this.getClass().getSimpleName(), message);
    }

    public void onException(Exception e) {
        Throwable t = Throwables.getRootCause(e);

        future.setException(t.getMessage());

        error(t);
    }

    private void error(Throwable t) {
        logger.error("handleEvent:error, event:" + this.getClass().getSimpleName() + ", exception:" + t.getMessage(), t);
    }

    public T getModule() {
        return module;
    }

    public boolean hasModule() {
        return module != null;
    }

    public EventFuture getFuture() {
        return future;
    }

    public String toJson() {
        return JsonHelper.toJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
