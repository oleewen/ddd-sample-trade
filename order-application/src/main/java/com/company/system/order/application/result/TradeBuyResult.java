package com.company.system.order.application.result;

import com.company.system.order.domain.model.Order;

/**
 * 交易下单结果对象
 *
 * @author only
 * @date 2020-05-22
 */
public class TradeBuyResult {
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

    public static TradeBuyResult create(Order order) {
        TradeBuyResult result = new TradeBuyResult();
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
