package com.company.businessdomain.goods.domain.model;

import com.company.businessdomain.common.domain.MonetaryAmount;

/**
 * 商品价格（值对象）
 *
 * @author only
 * @since 2020-05-22
 */
public class Price {
    /** 价格 */
    private MonetaryAmount amount;

    private Price(MonetaryAmount amount) {
        this.amount = amount;
    }

    public static Price create(int price) {
        MonetaryAmount amount = MonetaryAmount.create(price);

        return new Price(amount);
    }

    public MonetaryAmount calculateAmount(Integer count) {
        long price = amount.getCent();

        if (Long.MAX_VALUE / price < count) {
            throw new IllegalArgumentException("amount is overflow");
        }

        return MonetaryAmount.create(price * count);
    }
}
