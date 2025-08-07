package com.kylin.order.service.factory;

import com.kylin.order.api.module.dto.OrderBuyDTO;
import com.kylin.order.api.module.response.OrderBuyResponse;
import com.kylin.order.application.result.OrderBuyResult;

public class OrderResultFactory {
    public static OrderBuyResponse asResponse(OrderBuyResult buyResult) {
        OrderBuyResponse response = OrderBuyResponse.success();
        OrderBuyDTO buyDTO = OrderBuyDTOFactory.INSTANCE.toDTO(buyResult);
        response.setModule(buyDTO);
        return response;
    }
}
