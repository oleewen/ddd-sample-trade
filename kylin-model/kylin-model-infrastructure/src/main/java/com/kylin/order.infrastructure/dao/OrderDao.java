package com.kylin.order.infrastructure.dao;

import com.kylin.order.domain.model.Order;
import com.kylin.order.domain.model.OrderId;
import com.kylin.order.domain.repository.OrderRepository;
import com.kylin.order.infrastructure.entity.OrderEntity;
import org.springframework.ext.common.aspect.Call;
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
    private com.kylin.order.infrastructure.mapper.OrderMapper orderMapper;

    @Override
    @Call
    public void create(Order order) {
        // 领域对象转数据对象
        OrderEntity entity = com.kylin.order.infrastructure.factory.OrderFactory.instance(order);

        Long id = orderMapper.insert(entity);

        entity.setId(id);
        order.setOrderId(OrderId.create(id));
    }

    @Override
    @Call
    public boolean enable(Order order) {
        // 领域对象转数据对象
        OrderEntity entity = com.kylin.order.infrastructure.factory.OrderFactory.instance(order);

        // 更新数据库
        int count = orderMapper.enable(entity);

        // 设置成功
        if (count > 0) {
            return true;
        }

        throw new IllegalStateException("order enable failure");
    }
}
