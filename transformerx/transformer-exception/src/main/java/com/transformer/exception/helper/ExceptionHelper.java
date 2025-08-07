package com.transformer.exception.helper;


import com.transformer.exception.NestedRuntimeException;
import com.transformer.exception.ThrowableRuntimeException;
import com.transformer.status.Status;

/**
 * Created by only on 2017/3/13.
 */
public class ExceptionHelper {
    private ExceptionHelper(){}

    public static ThrowableRuntimeException createThrowableRuntimeException(String errorMessage) {
        return createThrowableRuntimeException(NestedRuntimeException.parseMessage(errorMessage));
    }

    public static ThrowableRuntimeException createThrowableRuntimeException(Throwable t) {
        return new ThrowableRuntimeException(t);
    }

    public static ThrowableRuntimeException createThrowableRuntimeException(Status status) {
        return new ThrowableRuntimeException(status);
    }

    public static ThrowableRuntimeException createThrowableRuntimeException(Status status, Throwable t) {
        return new ThrowableRuntimeException(status, t);
    }

    public static ThrowableRuntimeException createThrowableRuntimeException(String errorCode, String message) {
        return createThrowableRuntimeException(Status.DEFAULT_STATUS, errorCode, message);
    }

    public static ThrowableRuntimeException createThrowableRuntimeException(int status, String errorCode, String message) {
        return new ThrowableRuntimeException(status, errorCode, message);
    }

    public static ThrowableRuntimeException createThrowableRuntimeException(int status, String errorCode, String message, Throwable t) {
        return new ThrowableRuntimeException(status, errorCode, message, t);
    }

    public static NestedRuntimeException createNestedException(String errorMessage) {
        return createNestedException(NestedRuntimeException.parseMessage(errorMessage));
    }

    public static NestedRuntimeException createNestedException(Throwable t) {
        return new NestedRuntimeException(t);
    }

    public static NestedRuntimeException createNestedException(Status status) {
        return new NestedRuntimeException(status);
    }

    public static NestedRuntimeException createNestedException(Status status, Throwable t) {
        return new NestedRuntimeException(status, t);
    }

    public static NestedRuntimeException createNestedException(String errorCode, String message) {
        return createNestedException(Status.DEFAULT_STATUS, errorCode, message);
    }

    public static NestedRuntimeException createNestedException(int status, String errorCode, String message) {
        return new NestedRuntimeException(status, errorCode, message);
    }

    public static NestedRuntimeException createNestedException(int status, String errorCode, String message, Throwable t) {
        return new NestedRuntimeException(status, errorCode, message, t);
    }
}
