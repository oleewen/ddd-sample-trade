package com.company.department.business.common.resource.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 数据源
 *
 * @author only
 * @since 2020-05-22
 */
@Configuration
public class DataSourceConfig {
    @Value("${db.url}")
    private String promotionDBUrl;
    @Value("${db.username}")
    private String promotionDBUsername;
    @Value("${db.password}")
    private String promotionDBPassword;
    @Value("${db.maxactive}")
    private int promotionDBmaxActive;
    @Value("${db.minidle}")
    private int promotionDBminIdle;

    @Bean("dataSource")
    public DataSource buildDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(promotionDBUrl);
        dataSource.setUsername(promotionDBUsername);
        dataSource.setPassword(promotionDBPassword);
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");

        dataSource.setInitialSize(promotionDBminIdle);
        dataSource.setMaxActive(promotionDBmaxActive);
        dataSource.setMinIdle(promotionDBminIdle);

        dataSource.setTimeBetweenEvictionRunsMillis(30000);

        dataSource.setMinEvictableIdleTimeMillis(15000);

        dataSource.setValidationQuery("select 1");
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);

        return dataSource;

    }

}

