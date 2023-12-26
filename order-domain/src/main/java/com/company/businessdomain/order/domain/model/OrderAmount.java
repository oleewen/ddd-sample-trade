package com.company.businessdomain.order.domain.model;

import com.company.businessdomain.common.domain.MonetaryAmount;
import com.company.businessdomain.common.domain.ValueObject;
import lombok.Getter;

/**
 * 订单金额
 *
 * @author only
 * @since 2020-05-22
 */
@Getter
public class OrderAmount implements ValueObject<Long> {
    /** 订单金额 */
    private MonetaryAmount amount;

    private OrderAmount(MonetaryAmount amount) {
        this.amount = amount;
    }

    /**
     * 创建订单金额（工厂方法）
     *
     * @param amount 金额
     * @return 订单金额
     */
    public static OrderAmount create(MonetaryAmount amount) {
        return new OrderAmount(amount);
    }

    @Override
    public Long value() {
        return amount.getCent();
    }
}
