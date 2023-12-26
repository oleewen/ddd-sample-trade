package com.company.businessdomain.order.client;

import com.company.businessdomain.order.api.OrderService;
import com.company.businessdomain.order.api.module.request.OrderBuyRequest;
import com.company.businessdomain.order.api.module.response.OrderBuyResponse;

public class OrderClient implements OrderService {
    @Override
    public OrderBuyResponse buy(OrderBuyRequest orderRequest) {
        throw new UnsupportedOperationException("unimplemented operation");
    }
}

