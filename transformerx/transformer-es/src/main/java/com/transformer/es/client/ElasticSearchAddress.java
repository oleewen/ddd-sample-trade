package com.transformer.es.client;

import lombok.Data;

/**
 * 描述：es配置信息
 * 时间：2019-04-26 11:04
 */
@Data
public class ElasticSearchAddress {
    private String ipAddress;
    private int port;
}
