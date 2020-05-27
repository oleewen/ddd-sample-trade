package com.company.department.business.goods.resource.factory;

import com.company.department.business.goods.domain.model.Goods;
import com.company.department.business.goods.domain.model.GoodsId;
import com.company.department.business.goods.domain.model.Price;
import com.company.department.business.goods.resource.entity.GoodsEntity;

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
