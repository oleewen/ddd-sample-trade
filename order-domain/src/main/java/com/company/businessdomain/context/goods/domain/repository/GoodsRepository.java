package com.company.businessdomain.context.goods.domain.repository;

import com.company.businessdomain.context.goods.domain.model.Goods;

/**
 * 商品资源库
 *
 * @author only
 * @since 2020-05-22
 */
public interface GoodsRepository {
    Goods acquireGoods(Long goodsId);
}
