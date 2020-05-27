package com.company.department.business.order.domain.model;

/**
 * 订单状态
 *
 * @author only
 * @since 2020-05-22
 */
public enum OrderStatus {
    // 新创建
    NEW(0),
    // 已下单
    CREATED(1),
    // 已支付
    PAID(2),
    // 已收货
    CHECKED(3),
    // 已退款
    REFUND(4),
    // 已取消
    CANCELED(5),
    // 已关闭
    CLOSED(6),
    // 未定义
    NONE(-1);

    private int status;

    OrderStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public int value() {
        return getStatus();
    }

    public OrderStatus create() {
        if (this != NEW) {
            throw new IllegalStateException("status isn't " + NEW.name());
        }
        return CREATED;
    }
}
