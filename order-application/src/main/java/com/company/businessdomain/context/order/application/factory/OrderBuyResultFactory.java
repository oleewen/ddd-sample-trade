package com.company.businessdomain.context.order.application.factory;

import com.company.businessdomain.context.order.application.result.OrderBuyResult;
import com.company.businessdomain.context.order.domain.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * OrderBuyResult工厂方法
 * @author only
 * @date 2022-05-27
 */
@Mapper
public interface OrderBuyResultFactory {
    OrderBuyResultFactory INSTANCE = Mappers.getMapper(OrderBuyResultFactory.class);

    @Mapping(source = "order.orderId.value", target = "orderId")
    @Mapping(source = "order.goodsId.value", target = "goodsId")
    @Mapping(source = "order.buyerId.value", target = "buyerId")
    @Mapping(source = "order.sellerId.value", target = "sellerId")
    @Mapping(source = "order.amount.value", target = "amount")
    @Mapping(source = "order.status.value", target = "status")
    OrderBuyResult toResult(Order order);
}
