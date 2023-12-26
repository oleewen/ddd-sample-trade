package com.company.businessdomain.goods.infrastructure.call;

import com.company.businessdomain.goods.infrastructure.entity.GoodsEntity;
import org.springframework.stereotype.Repository;

/**
 * 商品服务
 *
 * @author only
 * @since 2020-05-22
 */
@Repository
public interface GoodsService {
    GoodsEntity getGoodsById(Long goodsId);
}
