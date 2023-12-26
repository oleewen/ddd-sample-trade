package com.company.businessdomain.goods.domain.model;

import com.company.businessdomain.common.domain.Id;

/**
 * 商品id（值对象）
 *
 * @author only
 * @since 2020-05-22
 */
public class GoodsId extends Id {
    private GoodsId(Long id) {
        super(id);
    }

    public static GoodsId create(Long id) {
        return new GoodsId(id);
    }
}
