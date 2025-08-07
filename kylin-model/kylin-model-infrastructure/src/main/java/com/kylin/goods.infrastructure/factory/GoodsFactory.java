package com.kylin.goods.infrastructure.factory;

import com.kylin.goods.domain.model.Goods;
import com.kylin.goods.domain.model.GoodsId;
import com.kylin.goods.domain.model.Price;
import com.kylin.goods.infrastructure.entity.GoodsEntity;

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
