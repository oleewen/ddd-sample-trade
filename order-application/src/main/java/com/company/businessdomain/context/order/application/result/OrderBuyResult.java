package com.company.businessdomain.context.order.application.result;

import com.company.businessdomain.context.order.domain.model.Order;

/**
 * 交易下单结果对象
 *
 * @author only
 * @date 2020-05-22
 */
public class OrderBuyResult {
    /** 订单id */
    private Long orderId;
    /** 商品id */
    private Long goodsId;
    /** 买家id */
    private Long buyerId;
    /** 卖家id */
    private Long sellerId;
    /** 购买件数 */
    private Integer itemCount;
    /** 订单金额 */
    private Long amount;
    /** 订单状态 */
    private Integer status;

    public static OrderBuyResult create(Order order) {
        OrderBuyResult result = new OrderBuyResult();
        result.orderId = order.getOrderId().value();
        result.goodsId = order.getGoodsId().value();
        result.buyerId = order.getBuyerId().value();
        result.sellerId = order.getSellerId().value();
        result.itemCount = order.getItemCount();
        result.amount = order.getAmount().value();
        result.status = order.getStatus().value();

        return result;
    }
}
