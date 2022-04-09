package com.company.businessdomain.context.order.service.web.reponse;

import com.company.businessdomain.context.order.application.result.OrderBuyResult;
import com.company.businessdomain.context.common.enums.StatusCode;
import org.springframework.ext.common.object.Status;
import org.springframework.ext.module.response.Response;

/**
 * 交易下单结果
 *
 * @author only
 * @since 2020-05-22
 */
public class OrderBuyResponse extends Response<OrderBuyResult> {
    public OrderBuyResponse(Status status) {
        super(status);
    }

    public static OrderBuyResponse valueOf(OrderBuyResult result) {
        OrderBuyResponse response = new OrderBuyResponse(StatusCode.SERVICE_RUN_SUCCESS);
        response.setModule(result);
        return response;
    }

    public static OrderBuyResponse empty() {
        return new OrderBuyResponse(StatusCode.PARAMETER_VALIDATE_ILLEGAL);
    }
}
