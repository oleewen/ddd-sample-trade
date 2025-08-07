package com.transformer.dao.mybatis.plugin;


import com.transformer.dao.config.TransformerDaoConfig;
import com.transformer.dao.exception.DangerousSQLException;

import com.transformer.dao.mybatis.plugin.model.NoWhereSQLInterceptorConfig;
import lombok.extern.slf4j.Slf4j;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;


import java.sql.Connection;
import java.util.Properties;

import static com.transformer.dao.enums.InterceptDispositionEnum.*;

/**
 * 无where-SQL拦截器
 * <p>拦截的对象：</p>
 *
 * <p>2、拦截无where和limit全表扫描查询</p>
 * <br/>
 * <p>拦截的方式：</p>
 * <p>1、仅打印warn日志：拦截到异常SQL后，打印warn级别日志，zcat不会告警，zlog可以查询</p>
 * <p>2、仅打印error日志：拦截到异常SQL后，打印error级别日志，zcat会告警</p>
 * <p>3、打印error和阻断执行：拦截到异常SQL后，打印error级别日志，zcat会告警，同时阻断当前SQL执行，避免全表查询</p>
 * <br/>
 * <p>扩展：</p>
 * <p>1、支持白名单配置、白名单注解（白名单不进行拦截）</p>
 *
 *
 * @author caokai01
 * @date 2022/8/29
 */
@Intercepts({
//        @Signature(type = Executor.class, method = "query",
//                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
////      update 实际会对 insert update delete 都有效
//        @Signature(type = Executor.class, method = "update",
//                args = {MappedStatement.class, Object.class}),
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})

})
@Slf4j
public class NowhereSQLInterceptor implements Interceptor {

    /**
     *   配置
     */
    @Autowired
    private TransformerDaoConfig transformerDaoConfig;

    /**
     * SQL关键字
     */
    private static final String SQL_WHERE = " where "; //注意带前后空格
    private static final String SQL_WHERE_1_EQ_1 = "where1=1";
    private static final String SQL_WHERE_1_EQ_1_AND = "where1=1and";
    private static final String SQL_SELECT = "select "; //注意带前空格
    private static final String SQL_UPDATE = "update "; //注意带前空格
    private static final String SQL_DELETE = "delete "; //注意带前空格
    private static final String SQL_INSERT = "insert "; //注意带前空格
    private static final String SQL_LIMIT = " limit "; //注意带前后空格  // mysql风格 分页
    private static final String SQL_ROWS_ONLY = "rows only"; //注意带前后空格  // oracle旧风格分页：WHERE ROWNUM <= 20 oracle新风格分页 ：OFFSET 10 ROWS FETCH NEXT 10 ROWS ONLY
    private static final String SQL_FROM = " from ";
    private static final String SQL_DUAL = "from dual";//oracle风格  查询序列
    private static final String BLANK = "\\s+";//空白字符正则表达  （包括空格、制表符、换行符等）




    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        NoWhereSQLInterceptorConfig config = NoWhereSQLInterceptorConfig.getFromConfigJSON( transformerDaoConfig.getNoWhereSQLConfig() )  ;

        handlerBefore(config,invocation);

        return invocation.proceed();
    }




    private void handlerBefore(NoWhereSQLInterceptorConfig config, Invocation invocation) {

        boolean needDisPosition = false;
        String sql = null;

        try{
            //拿不到配置  什么都不做
            if (config==null || config.isClosed() ){
                return;
            }
//             // 这个写法 不确定在所有版本中是否有效
//             MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
//             boolean notSelectSQL = mappedStatement==null ||    SqlCommandType.SELECT != mappedStatement.getSqlCommandType();

            StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
            sql = statementHandler.getBoundSql().getSql().trim().toLowerCase();

            //无法解析sql 什么也不做
            if(StringUtils.isEmpty( sql)){
                return;
            }

            //是否白名单：（1：静态注解，2：动态配置）
            if (isInWhiteList(invocation,config)) {
                return;
            }

            //第一种情况  没有where  没有where 没有limit  没有 from dual (序列)
            boolean noWhere  = !sql.contains(SQL_WHERE)
                    && !sql.contains(SQL_LIMIT)
                    && !sql.contains(SQL_ROWS_ONLY)
                    && !sql.contains(SQL_DUAL) ;

            //第二种情况 无效where : 有where 无 limit
            String sqlWithoutBlank = sql.replaceAll(BLANK, "");

            boolean unableWhere  = sql.contains(SQL_WHERE)
                    && !sql.contains(SQL_LIMIT)
                    && !sql.contains(SQL_ROWS_ONLY)
                    && sqlWithoutBlank.contains(SQL_WHERE_1_EQ_1)
                    && !sqlWithoutBlank.contains(SQL_WHERE_1_EQ_1_AND);

            //需要出发处理
            needDisPosition = noWhere ||  unableWhere;

        }catch (Throwable throwable){
            log.error("handlerBefore-ERROR",throwable );
        }

        if (needDisPosition){
            doDisPosition(sql,config);
        }



    }


    private SqlCommandType getSqlCommandType(final  String sql){
        if (sql==null){
            return SqlCommandType.UNKNOWN;
        }
        String sqlStr = sql.trim().toLowerCase();
        // 判断 SQL 类型
        if (sqlStr.startsWith(SQL_SELECT)) {
            return SqlCommandType.SELECT;
        }
        if (sqlStr.startsWith(SQL_UPDATE)) {
            return SqlCommandType.UPDATE;
        }
        if (sqlStr.startsWith(SQL_DELETE)) {
            return SqlCommandType.DELETE;
        }
        if (sqlStr.startsWith(SQL_INSERT)) {
            return SqlCommandType.INSERT;
        }
        return SqlCommandType.UNKNOWN;

    }


    /**
     * 高危SQL拦截处理
     * @param sql
     */
    private void doDisPosition(String sql, NoWhereSQLInterceptorConfig config ) {
        if ( config==null||  config.isClosed() || StringUtils.isEmpty(sql) ){
            return;
        }
        SqlCommandType sqlCommandType =  getSqlCommandType(sql);

        if (INTERCEPT_DEL.contains(config.getDispositions())){
            if ( SqlCommandType.DELETE.equals(sqlCommandType)){
                log.info("高危sql，无where和limit sql = {}", sql);
                throw new DangerousSQLException("高危SQL,SQL:" + sql);
            }
        }
        if (INTERCEPT_SELECT.contains(config.getDispositions())){
            if ( SqlCommandType.SELECT.equals(sqlCommandType)){
                log.info("高危sql，无where和limit sql = {}", sql);
                throw new DangerousSQLException("高危SQL,SQL:" + sql);
            }
        }
        if (INTERCEPT_UPDATE.contains(config.getDispositions())){
            if ( SqlCommandType.UPDATE.equals(sqlCommandType)){
                log.info("高危sql，无where和limit sql = {}", sql);
                throw new DangerousSQLException("高危SQL,SQL:" + sql);
            }
        }
        if (ERROR_LOG.contains(config.getDispositions())){
            log.error("高危sql，无where和limit sql = {}", sql);
            return;
        }
        if (WARN_LOG.contains(config.getDispositions())){
            log.warn("高危sql，无where和limit sql = {}", sql);
            return;

        }



    }




    /**
     * 是否为白名单，
     *
     * @param invocation
     * @return
     */
    private boolean isInWhiteList(Invocation invocation, NoWhereSQLInterceptorConfig config) {
        if (config==null ||  config.isClosed() ||   config.getWhiteList()==null  || config.getWhiteList().isEmpty() ){
            return false;
        }
        if (invocation.getArgs() == null ||  !( invocation.getArgs()[0] instanceof MappedStatement) ){
            return false;
        }
        // 这里有点问题
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        if (mappedStatement== null || StringUtils.isEmpty(mappedStatement.getId())){
            return false;
        }
        return config.getWhiteList().contains(  mappedStatement.getId()  );

    }


}
