package com.transformer.es.client;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 作者：WangLei
 * 描述：ES公共类型
 * 时间：2019-05-21 22:36
 */
@Data
public class ElasticSearchIndex implements Serializable {

    private static final long serialVersionUID = -1L;

    // 索引名称
    private String indexName;

    // id
    private String id;

    // 索引状态：0 初始｜1 启用｜2 重建中｜-1 禁用
    private int status;

    // json数据（不支持bulk模式）
    private String jsonData;

    // map数据（支持bulk模式）
    private Map<String, Object> sourceMap;

    public ElasticSearchIndex(String indexName) {
        this.indexName = indexName;
    }

}
