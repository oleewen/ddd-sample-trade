package com.company.businessdomain.context.order.service.factory;

import com.company.businessdomain.context.common.enums.StatusCode;
import com.company.businessdomain.context.order.api.module.dto.OrderBuyDTO;
import com.company.businessdomain.context.order.api.module.response.OrderBuyResponse;
import com.company.businessdomain.context.order.application.result.OrderBuyResult;
import org.springframework.ext.common.helper.BeanHelper;

public class OrderResultFactory {
    public static OrderBuyResponse asResponse(OrderBuyResult buyResult) {
        OrderBuyResponse response = new OrderBuyResponse(StatusCode.SERVICE_RUN_SUCCESS);
        OrderBuyDTO buyDTO = asDTO(buyResult);
        response.setModule(buyDTO);
        return response;
    }

    public static OrderBuyDTO asDTO(OrderBuyResult buyResult) {
        OrderBuyDTO module = BeanHelper.copyProperties(new OrderBuyDTO(), buyResult);
        return module;
    }
}
