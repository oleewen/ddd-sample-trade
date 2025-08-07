package com.transformer.status;

/**
 * 状态接口
 * User: only
 * Date: 14-6-21
 * Time: 下午10:15
 */
public interface Status {
    int DEFAULT_STATUS = 0;

    /**
     * 是否成功状态
     *
     * @return 成功状态，返回true；否则，返回false
     */
    boolean isSuccess();

    /**
     * 状态码值
     *
     * @return 状态码值
     */
    int getStatus();

    /**
     * 错误码
     *
     * @return 错误码
     */
    String getStatusCode();

    /**
     * 状态描述
     *
     * @return 状态描述
     */
    String getMessage();

    /**
     * 状态描述
     *
     * @return 状态描述
     */
    String getMessage(Object... format);
}
