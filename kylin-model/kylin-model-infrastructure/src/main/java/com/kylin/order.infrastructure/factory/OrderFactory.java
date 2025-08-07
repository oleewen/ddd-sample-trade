package com.kylin.order.infrastructure.factory;

import com.kylin.order.domain.model.Order;
import com.kylin.order.infrastructure.entity.OrderEntity;

/**
 * 订单工厂
 *
 * @author only
 * @since 2020-05-22
 */
public class OrderFactory {
    public static OrderEntity instance(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getOrderId().value());
        entity.setGoodsId(order.getGoodsId().value());
        entity.setBuyerId(order.getBuyerId().value());
        entity.setSellerId(order.getSellerId().value());
        entity.setAmount(order.getAmount().value());
        entity.setStatus(order.getStatus().value());

        return entity;
    }


}
