package com.company.system.order.domain.service;

import com.company.system.goods.domain.model.Goods;
import com.company.system.order.domain.model.Order;
import com.company.system.order.domain.repository.OrderRepository;
import com.company.system.user.domain.model.BuyerId;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 订单领域服务
 *
 * @author only
 * @date 2020-05-22
 */
@Component
public class OrderDomainService {
    /** 订单资源库 */
    @Resource
    private OrderRepository orderRepository;

    public Order create(Long buyerId, Goods goods, Integer count) {
        /** 创建交易订单 */
        Order order = Order.create(BuyerId.create(buyerId), goods, count);
        // 交易订单持久化
        orderRepository.create(order);

        return order;
    }

    public Order enable(Order order) {
        /** 设置订单可见 */
        order.enable();
        // 交易订单可见持久化
        orderRepository.enable(order);

        return order;
    }
}
