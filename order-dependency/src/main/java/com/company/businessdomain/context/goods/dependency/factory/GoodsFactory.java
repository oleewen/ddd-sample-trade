package com.company.businessdomain.context.goods.dependency.factory;

import com.company.businessdomain.context.goods.dependency.entity.GoodsEntity;
import com.company.businessdomain.context.goods.domain.model.Goods;
import com.company.businessdomain.context.goods.domain.model.GoodsId;
import com.company.businessdomain.context.goods.domain.model.Price;

/**
 * 商品工厂
 *
 * @author only
 * @since 2020-05-22
 */
public class GoodsFactory {

    public static Goods valueOf(GoodsEntity entity) {
        GoodsId id = GoodsId.create(entity.getId());
        Price price = Price.create(entity.getPrice());

        return new Goods(id, entity.getTitle(), price);
    }
}
