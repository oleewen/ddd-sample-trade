package com.company.businessdomain.context.user.domain.model;

/**
 * 买家id
 *
 * @author only
 * @since 2020-05-22
 */
public class BuyerId extends UserId {
    private BuyerId(Long id) {
        super(id);
    }

    public static BuyerId create(Long id) {
        return new BuyerId(id);
    }
}
