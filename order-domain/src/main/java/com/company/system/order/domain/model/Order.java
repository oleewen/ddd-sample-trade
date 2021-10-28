package com.company.system.order.domain.model;

import com.company.system.common.domain.MonetaryAmount;
import com.company.system.goods.domain.model.Goods;
import com.company.system.goods.domain.model.GoodsId;
import com.company.system.user.domain.model.BuyerId;
import com.company.system.user.domain.model.SellerId;
import lombok.Getter;
import lombok.Setter;

/**
 * 订单（聚合）
 *
 * @author only
 * @since 2020-05-22
 */
@Getter
public class Order {
    @Setter
    /** 订单id */
    private OrderId orderId;
    /** 商品id */
    private GoodsId goodsId;
    /** 买家id */
    private BuyerId buyerId;
    /** 卖家id */
    private SellerId sellerId;
    /** 购买件数 */
    private Integer itemCount;
    /** 订单金额 */
    private OrderAmount amount;
    /** 订单状态 */
    private OrderStatus status;

    /**
     * 创建订单（工厂方法）
     * <pre>
     * 创建买家buyerId购买了count件商品goods的订单
     * 默认订单状态为新创建
     * </pre>
     *
     * @param buyerId   买家id
     * @param goods     商品
     * @param itemCount 商品件数
     *
     * @return 订单对象
     *
     * @see OrderAmount
     * @see OrderStatus
     */
    public static Order create(BuyerId buyerId, Goods goods, Integer itemCount) {
        Order order = new Order();
        order.goodsId = goods.getGoodsId();
        order.buyerId = buyerId;
        order.sellerId = goods.getSellerId();
        order.itemCount = itemCount;
        // 计算原价
        MonetaryAmount amount = goods.calculateAmount(itemCount);
        order.amount = OrderAmount.create(amount);

        order.status = OrderStatus.NEW;

        return order;
    }


    public void enable() {
        if (status == null) {
            throw new IllegalStateException("status is null");
        }
        status = status.create();
    }

}
