package com.company.system.order.resource.dao;

import com.company.system.order.domain.model.Order;
import com.company.system.order.domain.model.OrderId;
import com.company.system.order.domain.repository.OrderRepository;
import com.company.system.order.resource.entity.OrderEntity;
import com.company.system.order.resource.factory.OrderFactory;
import com.company.system.order.resource.mapper.OrderMapper;
import org.springframework.ext.common.aspect.Call;
import org.springframework.ext.common.exception.ExceptionHelper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * 订单数据访问对象
 *
 * @author only
 * @since 2020-05-22
 */
@Repository
public class OrderDao implements OrderRepository {
    /** 订单Mapper */
    @Resource
    private OrderMapper orderMapper;

    @Override
    @Call(status = 20001, errorCode = "TRADE_ORDER_CREATE_EXCEPTION", errorMessage = "订单创建异常")
    public void create(Order order) {
        // 领域对象转数据对象
        OrderEntity entity = OrderFactory.instance(order);

        Long id = orderMapper.insert(entity);

        entity.setId(id);
        order.setOrderId(OrderId.create(id));
    }

    @Override
    @Call(status = 20002, errorCode = "TRADE_ORDER_ENABLE_EXCEPTION", errorMessage = "订单下单失败")
    public boolean enable(Order order) {
        // 领域对象转数据对象
        OrderEntity entity = OrderFactory.instance(order);

        // 更新数据库
        int count = orderMapper.enable(entity);

        // 设置成功
        if (count > 0) {
            return true;
        }

        throw ExceptionHelper.createNestedException(20003, "TRADE_ORDER_ENABLE_FAILURE", "订单下单失败");
    }
}
