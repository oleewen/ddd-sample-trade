package com.kylin.order.domain.model;

import com.kylin.common.domain.Id;

/**
 * 订单id（值对象）
 *
 * @author only
 * @since 2020-05-22
 */
public class OrderId extends Id {
    private OrderId(Long id) {
        super(id);
    }

    public static com.kylin.order.domain.model.OrderId create(Long id) {
        return new com.kylin.order.domain.model.OrderId(id);
    }
}
