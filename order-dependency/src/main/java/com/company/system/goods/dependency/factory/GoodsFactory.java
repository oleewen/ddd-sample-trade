package com.company.system.goods.dependency.factory;

import com.company.system.goods.domain.model.Goods;
import com.company.system.goods.domain.model.GoodsId;
import com.company.system.goods.domain.model.Price;
import com.company.system.goods.dependency.entity.GoodsEntity;

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
