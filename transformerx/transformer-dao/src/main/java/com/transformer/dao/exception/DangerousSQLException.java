package com.transformer.dao.exception;

/**
 * 高危sql异常
 * @author caokai01
 * @date 2022/9/29
 */
public class DangerousSQLException extends RuntimeException{

    public DangerousSQLException() {
        super();
    }

    public DangerousSQLException(String message) {
        super(message);
    }
}
