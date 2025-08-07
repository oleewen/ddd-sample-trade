package com.transformer.dao.mybatis.plugin.model;


import com.transformer.dao.enums.InterceptDispositionEnum;
import com.transformer.helper.JsonHelper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * 危险SQL拦截器
 * @author caokai01
 * @date 2022/9/29
 */
@Slf4j
@Data
public class NoWhereSQLInterceptorConfig {

    /**
     * 是否启用   true 开启 ，false 关闭  默认关闭
     */
    private Boolean enable = false;

    /**
     *  操作
     */
    private  List<String> dispositions;


    /**
     *  白名单
     */
    private List<String> whiteList ;

    /**
     *  是否 不启用
     * @return
     */
    public boolean  isClosed(){
        return  enable==null
                ||  Boolean.FALSE.equals(enable)
                ||  dispositions ==null
                ||  dispositions.isEmpty();
    }


    public static NoWhereSQLInterceptorConfig  getDefault(){
        NoWhereSQLInterceptorConfig config = new NoWhereSQLInterceptorConfig() ;
        config.setEnable(false);
        config.setDispositions(Collections.singletonList(InterceptDispositionEnum.ERROR_LOG.getCode()));
        return  config;
    }

    public static NoWhereSQLInterceptorConfig getFromConfigJSON(String configJSON){
        if ( configJSON==null  || configJSON.trim().isEmpty()) {
            return getDefault();
        }
        try{
            return   JsonHelper.parseObject(configJSON,NoWhereSQLInterceptorConfig.class);
        }catch (Throwable throwable){
            return getDefault();
        }
    }







}
