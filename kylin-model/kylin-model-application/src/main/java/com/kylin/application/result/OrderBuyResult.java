package com.kylin.order.application.result;

import lombok.Data;

/**
 * 交易下单结果对象
 *
 * @author only
 * @date 2020-05-22
 */
@Data
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
}
