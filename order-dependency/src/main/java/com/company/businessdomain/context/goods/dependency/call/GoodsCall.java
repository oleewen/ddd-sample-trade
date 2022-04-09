package com.company.businessdomain.context.goods.dependency.call;

import com.company.businessdomain.context.goods.dependency.entity.GoodsEntity;
import org.springframework.stereotype.Repository;

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
