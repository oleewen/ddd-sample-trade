package com.transformer.dao.mybatis.plugin;


import com.transformer.dao.config.TransformerDaoConfig;
import com.transformer.dao.mybatis.plugin.model.BigObjQueryInterceptorConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Properties;

import static com.transformer.dao.enums.InterceptDispositionEnum.*;

/**
 * 大对象查询拦截器

 * <p>1、拦截大对象慢查询</p>

 * <br/>
 * <p>拦截的方式：</p>
 * <p>1、仅打印warn日志：拦截到异常SQL后，打印warn级别日志，zcat不会告警，zlog可以查询</p>
 * <p>2、仅打印error日志：拦截到异常SQL后，打印error级别日志，zcat会告警</p>
 * <br/>
 * <p>扩展：</p>
 * <p>1、支持白名单配置、白名单注解（白名单不进行拦截）</p>
 *
 *
 * @author caokai01
 * @date 2022/8/29
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
})
@Slf4j
public class BigObjQueryInterceptor implements Interceptor {


    @Autowired
    private TransformerDaoConfig transformerDaoConfig;


    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        BigObjQueryInterceptorConfig config = BigObjQueryInterceptorConfig.getFromConfigJSON( transformerDaoConfig.getBigObjQueryConfig() )  ;

        Object proceed =   invocation.proceed();

        handlerAfter(config,invocation,proceed);

        return proceed;
    }

    private void handlerAfter(BigObjQueryInterceptorConfig config, Invocation invocation ,Object proceed) {
        try {
            if (config == null || config.isClosed()) {
                return;
            }
            if (!(proceed instanceof List)) {
                return;
            }
            List proceedList = (List) proceed;

            if ( proceed != null && proceedList.size() >= config.getThreshold()  ) {
                 doDisposition(config, invocation) ;
            }
        } catch (Throwable throwable) {
            log.error("handlerAfter-Error", throwable);
        }

    }

    private   void  doDisposition(BigObjQueryInterceptorConfig config, Invocation invocation) {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        String sql = statementHandler.getBoundSql().getSql();

        if (ERROR_LOG.contains(config.getDispositions())) {
            log.error("高危sql，无where和limit sql = {}", sql);
            return  ;
        }
        if (WARN_LOG.contains(config.getDispositions())) {
            log.warn("高危sql，无where和limit sql = {}", sql);
            return  ;
        }
        return  ;
    }


}
