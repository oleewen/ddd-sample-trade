package com.company.businessdomain.context.order.infrastructure.mapper;

import com.company.businessdomain.context.order.infrastructure.entity.OrderEntity;

/**
 * 订单Mapper
 *
 * @author only
 * @since 2020-05-22
 */
public interface OrderMapper {
    /**
     * 插入订单
     *
     * @param order 交易订单
     */
    Long insert(OrderEntity order);

    /**
     * 设置订单可见，关联商品订单id，支付订单id
     *
     * @param order 交易订单
     * @return 更新成功的记录数
     */
    int enable(OrderEntity order);
}
