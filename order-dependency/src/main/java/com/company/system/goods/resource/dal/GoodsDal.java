package com.company.system.goods.resource.dal;

import com.company.system.goods.domain.model.Goods;
import com.company.system.goods.domain.repository.GoodsRepository;
import com.company.system.goods.resource.call.GoodsCall;
import com.company.system.goods.resource.entity.GoodsEntity;
import com.company.system.goods.resource.factory.GoodsFactory;
import org.springframework.ext.common.aspect.Call;
import org.springframework.ext.common.exception.ExceptionHelper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * 商品数据访问层
 *
 * @author only
 * @since 2020-05-22
 */
@Repository
public class GoodsDal implements GoodsRepository {
    /** 商品服务 */
    @Resource
    private GoodsCall goodsCall;

    @Override
    @Call(status = 30001, errorCode = "TRADE_GOODS_GET_EXCEPTION", errorMessage = "查询商品异常")
    public Goods acquireGoods(Long goodsId) {
        GoodsEntity goods = goodsCall.getGoodsById(goodsId);

        if (goods != null) {
            return GoodsFactory.valueOf(goods);
        }

        throw ExceptionHelper.createNestedException(30002, "TRADE_GOODS_NOT_FOUND", "找不到商品");
    }
}
