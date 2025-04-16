package com.transformer.helper;

import com.transformer.exception.helper.ExceptionHelper;
import com.transformer.status.Status;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * 业务断言工具类
 */
public abstract class AssertHelper {

    public static void isNull(@Nullable final Object object, Status status) {
        isNull(object, status, null);
    }

    public static void isNull(@Nullable final Object object, Status status, Object... format) {
        if (object != null) {
            throwAssertException(status, format);
        }
    }

    public static void notNull(@Nullable final Object object, Status status) {
        notNull(object, status, null);
    }

    public static void notNull(@Nullable final Object object, Status status, Object... format) {
        if (object == null) {
            throwAssertException(status, format);
        }
    }

    public static void isTrue(final boolean expression, Status status) {
        isTrue(expression, status, null);
    }

    public static void isTrue(final boolean expression, Status status, Object... format) {
        if (!expression) {
            throwAssertException(status, format);
        }
    }

    public static void isFalse(final boolean expression, Status status) {
        isFalse(expression, status, null);
    }

    public static void isFalse(final boolean expression, Status status, Object... format) {
        if (expression) {
            throwAssertException(status, format);
        }
    }

    public static void isEmpty(@Nullable final Collection object, Status status, Object... format) {
        if (object != null && !object.isEmpty()) {
            throwAssertException(status, format);
        }
    }

    public static void isEmpty(@Nullable final Collection object, Status status) {
        isEmpty(object, status, null);
    }

    public static void isNotEmpty(@Nullable final Collection object, Status status) {
        isNotEmpty(object, status, null);
    }

    public static void isNotEmpty(@Nullable final Collection object, Status status, Object... format) {
        if (object == null || object.isEmpty()) {
            throwAssertException(status, format);
        }
    }

    public static void isNotBlank(@Nullable final String string, Status status) {
        isNotBlank(string, status, null);
    }

    public static void isNotBlank(@Nullable final String string, Status status, Object... format) {
        if (StringUtils.isBlank(string)) {
            throwAssertException(status, format);
        }
    }

    public static void isBlank(@Nullable final String string, Status status) {
        isBlank(string, status, null);
    }

    public static void isBlank(String target, Status status, Object... format) {
        if (StringUtils.isNotBlank(target)) {
            throwAssertException(status, format);
        }
    }

    public static void matches(@Nullable final String string, Pattern pattern, Status status) {
        matches(string, pattern, status, null);
    }

    public static void matches(@Nullable final String string, Pattern pattern, Status status, Object... format) {
        if (!pattern.matcher(string).matches()) {
            throwAssertException(status, format);
        }
    }

    private static void throwAssertException(Status status, Object... format) {
        throwAssertException(status.getStatusCode(), status.getMessage(), format);
    }

    private static void throwAssertException(String statusCode, String message, Object... format) {
        String msg = message;
        if (format != null && format.length > 0) {
            msg = MessageFormat.format(message, format);
        }
        throw ExceptionHelper.createNestedException(statusCode, msg);
    }

}