package com.transformer.log.aspect;

import com.google.common.base.Preconditions;
import com.transformer.log.annotation.Call;
import com.transformer.exception.helper.ExceptionHelper;
import com.transformer.exception.NestedRuntimeException;
import com.transformer.helper.JsonHelper;
import com.transformer.context.Context;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Random;

/**
 * 日志切面
 * <pre>
 * @Aspect
 * public class CallAspect extends CallAround {
 * }
 *
 * @Configuration
 * @EnableAspectJAutoProxy
 * @ComponentScan
 * public class SpringConfig {
 * @Bean
 * public CallAspect callAspect() {
 * return new CallAspect();
 * }
 * }
 * </pre>
 *
 * @author only
 * @date 2015-07-14
 */
public class CallAround {
    /** 随机采样频率 */
    private static Random random = new Random();

    @Pointcut("@annotation(com.zto.tms.transformer.log.aspect.Call)")
    public void callPoint() {
    }

    /**
     * profiler方法拦截
     *
     * @param joinPoint 连接点
     */
    @Around("callPoint()")
    public Object call(ProceedingJoinPoint joinPoint) throws Throwable {
        // 方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Call call = method.getAnnotation(Call.class);

        long start = System.currentTimeMillis();

        Object result = null;
        try {
            // 执行方法
            result = joinPoint.proceed();

            long end = System.currentTimeMillis();
            // 有@Call注解
            if (call != null) {
                // 打印日志
                logger(joinPoint, call, end - start, result);
            }
        } catch (Exception e) {
            long end = System.currentTimeMillis();
            // 打印日志
            error(joinPoint, call, end - start, e);

            throw handleException(call, e);
        }
        return result;
    }

    private void logger(ProceedingJoinPoint joinPoint, Call call, long elapsed, Object result) {
        Preconditions.checkNotNull(joinPoint, "joinPoint is null");
        Preconditions.checkNotNull(call, "call is null");

        Logger logger = getLogger(joinPoint, call);

        // 调用成功
        if (isSuccess(result)) {
            // 超时，打印日志
            if (elapsed > call.elapsed()) {
                logger.error(formatMessage(joinPoint, call, elapsed, "timeout", result));
            }
            // 符合采样频率条件
            else if (random.nextInt(call.basic()) <= call.sample()) {
                logger.warn(formatMessage(joinPoint, call, elapsed, "sample", result));
            }
            // debug日志
            else if (Context.debug()) {
                logger.warn(formatMessage(joinPoint, call, elapsed, "debug", result));
            }
            // 其他（未超时、未采用命中），不打印日志
            else {
                otherCaseLogger(joinPoint, call, elapsed, result);
            }
            return;
        }

        // 调用失败，访问日志
        logger.warn(formatMessage(joinPoint, call, elapsed, "failure", result));
    }

    protected boolean isSuccess(Object result) {
        return result != null;
    }

    private String formatMessage(ProceedingJoinPoint joinPoint, Call call, long elapsed, String status, Object result) {

        return String.format("%s@method:%s,args:%s,elapsed:%d;duration:%d,result:%s", status, joinPoint.getSignature().toShortString(), JsonHelper.toJson(joinPoint.getArgs()), call.elapsed(), elapsed, JsonHelper.toJson(result));
    }

    protected void otherCaseLogger(ProceedingJoinPoint joinPoint, Call call, long elapsed, Object result) {
        // 下层复写，可支持访问日志打印
    }

    private void error(ProceedingJoinPoint joinPoint, Call call, long elapsed, Exception e) {
        Logger logger = getLogger(joinPoint, call);

        /** 记录异常日志 */
        logger.error(formatMessage(joinPoint, call, elapsed, "exception", null), e);
    }

    private Throwable handleException(Call call, Exception e) {
        /** 重新抛出异常 */
        // 自定义内部异常，直接抛出
        if (e instanceof NestedRuntimeException) {
            return e;
        }
        // 系统内部异常，优先采用Call注解定义的业务码
        if (call.status() != 0 && StringUtils.isNotEmpty(call.errorCode())) {
            return ExceptionHelper.createNestedException(call.status(), call.errorCode(), call.errorMessage(), e);
        }
        // Call未定义业务码，包装为自定义内部错误码
        return ExceptionHelper.createNestedException(e);
    }

    private Logger getLogger(ProceedingJoinPoint joinPoint, Call call) {
        return LoggerFactory.getLogger(StringUtils.defaultIfBlank(call.value(), joinPoint.getClass().getName()));
    }
}
