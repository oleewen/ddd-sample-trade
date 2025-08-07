package com.transformer.dao.mybatis.plugin.model;


import com.transformer.dao.enums.InterceptDispositionEnum;
import com.transformer.helper.JsonHelper;
import com.transformer.helper.StringHelper;
import lombok.Data;
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
public class BigObjQueryInterceptorConfig {

    /**
     * 是否启用   true 开启 ，false 关闭  默认关闭
     */
    private Boolean enable = false;

    /**
     *  操作
     */
    private  List<String> dispositions;


        /**
     * 大对象行阈值
     */
    private Long threshold = 10000L;

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


    public static BigObjQueryInterceptorConfig getDefault(){
        BigObjQueryInterceptorConfig config = new BigObjQueryInterceptorConfig() ;
        config.setEnable(false);
        config.setDispositions(Collections.singletonList(InterceptDispositionEnum.WARN_LOG.getCode()));
        config.setThreshold(10000L);
        return  config;
    }

    public static BigObjQueryInterceptorConfig getFromConfigJSON(String configJSON){
        if ( configJSON==null || configJSON.trim().isEmpty()) {
            return getDefault();
        }
        try{
            return   JsonHelper.parseObject(configJSON, BigObjQueryInterceptorConfig.class);
        }catch (Throwable throwable){
            return getDefault();
        }
    }







}
