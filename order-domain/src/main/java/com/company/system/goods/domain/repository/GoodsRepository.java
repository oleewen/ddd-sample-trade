package com.company.system.goods.domain.repository;

import com.company.system.goods.domain.model.Goods;

/**
 * 商品资源库
 *
 * @author only
 * @since 2020-05-22
 */
public interface GoodsRepository {
    Goods acquireGoods(Long goodsId);
}
