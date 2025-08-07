package com.transformer.dao.config;


import com.transformer.dao.mybatis.plugin.model.BigObjQueryInterceptorConfig;
import com.transformer.dao.mybatis.plugin.model.NoWhereSQLInterceptorConfig;
import lombok.*;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 危险SQL拦截器
 * @author caokai01
 * @date 2022/9/29
 */

@ConfigurationProperties(prefix = "transformer.dao")
@Component("transformerDaoConfig")
@Data
public class TransformerDaoConfig {


    /**
     * {@link NoWhereSQLInterceptorConfig }
     */
    private String noWhereSQLConfig;

    /**
     * {@link BigObjQueryInterceptorConfig }
     */
    private String bigObjQueryConfig;



}
