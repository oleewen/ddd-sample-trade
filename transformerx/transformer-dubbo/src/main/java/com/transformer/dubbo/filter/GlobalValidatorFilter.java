package com.transformer.dubbo.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.transformer.exception.NestedRuntimeException;
import com.transformer.exception.helper.ExceptionHelper;
import com.transformer.status.ResultCodeEnum;

import com.zto.titans.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Activate(group = Constants.PROVIDER, order = Integer.MAX_VALUE)
@Configuration
public class GlobalValidatorFilter implements Filter, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try {
            // 参数校验
            Set<ConstraintViolation<Object>> validateResult = validate(invocation.getArguments());

            // 提取校验失败消息
            String validateMessage = extractValidateMessage(validateResult);

            // 封装校验失败消息到状态码和异常
            if (StringUtils.isNotBlank(validateMessage)) {
                ResultCodeEnum resultCode = ResultCodeEnum.BAD_REQUEST;
                NestedRuntimeException exception = ExceptionHelper.createNestedException(resultCode.getStatusCode(), resultCode.getMessage(validateMessage));

                return new RpcResult(exception);
            }
        } catch (Exception e) {
            log.error("validate arguments error:{}", JsonUtil.toJSON(invocation.getArguments()), e);
        }

        // 验证成功
        return invoker.invoke(invocation);
    }


    /**
     * 获取校验结果
     *
     * @param arguments
     */
    private Set<ConstraintViolation<Object>> validate(Object[] arguments) {
        if (ArrayUtils.isEmpty(arguments)) {
            return Collections.emptySet();
        }

        return Arrays.stream(arguments)
                .map(this::validate)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    /**
     * 获取校验结果
     *
     * @param argument
     */
    private Set<ConstraintViolation<Object>> validate(Object argument) {
        if (Objects.isNull(argument)) {
            return Collections.emptySet();
        }

        try {
            Validator validator = getValidator();
            return validator.validate(argument);
        } catch (Exception e) {
            // validator执行时的异常，不阻断操作
            log.error("validate error argument:{}", JsonUtil.toJSON(argument), e);
        }
        return Collections.emptySet();
    }

    /**
     * 获取validator校验器
     *
     * @return
     */
    private Validator getValidator() {
        return applicationContext.getBean(Validator.class);
    }

    /**
     * 提取所有校验失败的文案
     * @param validateResult
     * @return
     */
    private String extractValidateMessage(Set<ConstraintViolation<Object>> validateResult) {
        if (CollectionUtils.isEmpty(validateResult)) {
            return StringUtils.EMPTY;
        }

        return validateResult.stream()
                .map(ConstraintViolation::getMessage)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(";"));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
