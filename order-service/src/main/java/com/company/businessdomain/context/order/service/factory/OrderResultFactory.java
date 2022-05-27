package com.company.businessdomain.context.order.service.factory;

import com.company.businessdomain.context.order.api.module.dto.OrderBuyDTO;
import com.company.businessdomain.context.order.api.module.response.OrderBuyResponse;
import com.company.businessdomain.context.order.application.result.OrderBuyResult;

public class OrderResultFactory {
    public static OrderBuyResponse asResponse(OrderBuyResult buyResult) {
        OrderBuyResponse response = OrderBuyResponse.success();
        OrderBuyDTO buyDTO = OrderBuyDTOFactory.INSTANCE.toDTO(buyResult);
        response.setModule(buyDTO);
        return response;
    }
}
