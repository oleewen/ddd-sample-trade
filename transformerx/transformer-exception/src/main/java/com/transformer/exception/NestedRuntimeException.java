package com.transformer.exception;

import com.transformer.consts.StringConst;
import com.transformer.status.Status;
import com.zto.titans.common.exception.BusinessException;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Optional;

import static com.transformer.status.Status.DEFAULT_STATUS;

/**
 * @author only
 * @date 2017/7/10.
 */
public class NestedRuntimeException extends BusinessException {
    private final int status;
    static final String SPLIT = StringConst.IMARK;

    public NestedRuntimeException(Throwable throwable) {
        super(throwable, throwable.getMessage());
        this.status = DEFAULT_STATUS;
    }

    public NestedRuntimeException(Status status) {
        this(status.getStatus(), status.getStatusCode(), status.getMessage());
    }

    public NestedRuntimeException(Status status, Throwable throwable) {
        this(status.getStatus(), status.getStatusCode(), status.getMessage(), throwable);
    }

    public NestedRuntimeException(int status, String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
        this.status = status;
    }

    public NestedRuntimeException(int status, String errorCode, String errorMessage, Throwable throwable) {
        super(errorCode, errorMessage, throwable);
        this.status = status;
    }

    public static Status parseMessage(final String message) {
        String[] parts = Optional.ofNullable(message)
                .filter(msg -> msg.contains(SPLIT))
                .map(msg -> msg.split(SPLIT))
                .orElse(new String[]{"", "", ""});

        int status = parts.length > 0 && NumberUtils.isDigits(parts[0]) ? Integer.parseInt(parts[0]) : DEFAULT_STATUS;
        String errorCode = parts.length > 1 ? parts[1] : "";
        String errorMessage = parts.length > 2 ? parts[2] : "";

        return new Status() {
            @Override
            public boolean isSuccess() {
                return false;
            }

            @Override
            public int getStatus() {
                return status;
            }

            @Override
            public String getStatusCode() {
                return errorCode;
            }

            @Override
            public String getMessage() {
                return errorMessage;
            }

            @Override
            public String getMessage(Object... format) {
                return errorMessage;
            }
        };
    }

    public String getFormattedMessage() {
        return String.format("%s%s%s%s%s", getStatus(), SPLIT, getErrorCode(), SPLIT, getErrorMessage());
    }

    public int getStatus() {
        return this.status;
    }

    public String getErrorCode() {
        return getCode();
    }

    public String getErrorMessage() {
        return getMsg();
    }

}
