package com.company.businessdomain.order.infrastructure.entity;

import lombok.Data;

/**
 * 订单数据对象
 *
 * @author only
 * @since 2020-05-22
 */
@Data
public class OrderEntity {
    /** 订单id */
    private Long id;
    /** 商品id */
    private Long goodsId;
    /** 买家id */
    private Long buyerId;
    /** 卖家id */
    private Long sellerId;
    /** 订单金额 */
    private Long amount;
    /** 订单状态 */
    private int status;
}
