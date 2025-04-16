package com.kylin.goods.infrastructure.call;

import org.springframework.stereotype.Repository;
import com.kylin.goods.infrastructure.entity.GoodsEntity;

/**
 * 商品服务
 *
 * @author only
 * @since 2020-05-22
 */
@Repository
public interface GoodsCall {
    GoodsEntity getGoodsById(Long goodsId);
}
