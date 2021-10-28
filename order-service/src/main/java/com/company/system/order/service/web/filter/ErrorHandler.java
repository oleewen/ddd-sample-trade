package com.company.system.order.service.web.filter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.company.system.order.common.enums.StatusCode;
import org.springframework.ext.common.exception.NestedRuntimeException;
import org.springframework.ext.module.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author only
 * @since 2020-05-22
 */
@ControllerAdvice
@ResponseBody
public class ErrorHandler {
    private final static Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    @ExceptionHandler(NestedRuntimeException.class)
    public Response handlerBusinessException(NestedRuntimeException e) {
        logger.error(e.getErrorCode(), e);
        Response resp = Response.create(e);
        return resp;
    }

    @ExceptionHandler(JsonMappingException.class)
    public Response handlerException(JsonMappingException e) {
        logger.error(StatusCode.JSON_FORMAT_ERROR.getErrorCode(), e);
        Response resp = Response.create(StatusCode.JSON_FORMAT_ERROR);
        return resp;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Response handlerException(HttpMessageNotReadableException e) {
        logger.error(StatusCode.HTTP_ACCESS_ERROR.getErrorCode(), e);
        Response resp = Response.create(StatusCode.HTTP_ACCESS_ERROR);
        return resp;
    }

    @ExceptionHandler(JsonParseException.class)
    public Response handlerException(JsonParseException e) {
        logger.error(StatusCode.JSON_FORMAT_ERROR.getErrorCode(), e);
        Response resp = Response.create(StatusCode.JSON_FORMAT_ERROR);
        return resp;
    }

    @ExceptionHandler(InvalidFormatException.class)
    public Response handlerException(InvalidFormatException e) {
        logger.error("错误", e);
        Response resp = Response.create(StatusCode.DATA_FORMAT_ERROR);
        return resp;
    }

    @ExceptionHandler(Exception.class)
    public Response handlerException(Exception e) {
        logger.error(StatusCode.SERVICE_RUN_ERROR.getErrorCode(), e);
        return Response.create(StatusCode.SERVICE_RUN_ERROR);
    }
}

