package com.company.department.business.order.present.web.reponse;

import com.company.department.business.order.application.result.TradeBuyResult;
import com.company.department.business.order.common.enums.StatusCode;
import org.springframework.ext.common.object.Status;
import org.springframework.ext.module.response.Response;

/**
 * 交易下单结果
 *
 * @author only
 * @since 2020-05-22
 */
public class TradeBuyResponse extends Response<TradeBuyResult> {
    public TradeBuyResponse(Status status) {
        super(status);
    }

    public static TradeBuyResponse valueOf(TradeBuyResult result) {
        TradeBuyResponse response = new TradeBuyResponse(StatusCode.SERVICE_RUN_SUCCESS);
        response.setModule(result);
        return response;
    }

    public static TradeBuyResponse empty() {
        return new TradeBuyResponse(StatusCode.PARAMETER_VALIDATE_ILLEGAL);
    }
}
