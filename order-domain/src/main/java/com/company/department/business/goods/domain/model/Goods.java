package com.company.department.business.goods.domain.model;


import com.company.department.business.common.domain.MonetaryAmount;
import com.company.department.business.user.domain.model.SellerId;
import lombok.Getter;

/**
 * 商品（聚合）
 *
 * @author only
 * @since 2020-05-22
 */
@Getter
public class Goods {
    /** 商品id */
    private GoodsId goodsId;
    /** 商品标题 */
    private String title;
    /** 商品价格 */
    private Price price;
    /** 卖家id */
    private SellerId sellerId;

    public Goods(GoodsId id, String title, Price price) {
        this.goodsId = id;
        this.title = title;
        this.price = price;
    }

    /**
     * 计算商品总价
     *
     * <pre>
     * 商品总价 = 商品单价 x 商品件数
     * </pre>
     *
     * @param count 商品件数
     * @return 商品总价金额对象
     */
    public MonetaryAmount calculateAmount(Integer count) {
        return price.calculateAmount(count);
    }
}
