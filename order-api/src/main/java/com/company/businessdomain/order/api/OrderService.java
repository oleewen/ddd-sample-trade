package com.company.businessdomain.order.api;

import com.company.businessdomain.order.api.module.request.OrderBuyRequest;
import com.company.businessdomain.order.api.module.response.OrderBuyResponse;
import io.swagger.annotations.Api;

@Api(value = "下单服务", description = "下单服务")
public interface OrderService {
    OrderBuyResponse buy(OrderBuyRequest orderRequest);
}
