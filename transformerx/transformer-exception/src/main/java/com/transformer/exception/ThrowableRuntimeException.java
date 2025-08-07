package com.transformer.exception;

import com.transformer.status.Status;

/**
 * @author only
 * @since 2020/7/31
 */
public class ThrowableRuntimeException extends NestedRuntimeException {

    public ThrowableRuntimeException(Throwable t) {
        super(t);
    }

    public ThrowableRuntimeException(Status status) {
        super(status);
    }

    public ThrowableRuntimeException(Status status, Throwable cause) {
        super(status, cause);
    }

    public ThrowableRuntimeException(int status, String errorCode, String message) {
        super(status, errorCode, message);
    }

    public ThrowableRuntimeException(int status, String errorCode, String message, Throwable t) {
        super(status, errorCode, message, t);
    }
}
