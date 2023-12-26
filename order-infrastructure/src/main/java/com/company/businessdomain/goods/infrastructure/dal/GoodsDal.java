package com.company.businessdomain.goods.infrastructure.dal;

import com.company.businessdomain.goods.infrastructure.call.GoodsService;
import com.company.businessdomain.goods.infrastructure.entity.GoodsEntity;
import com.company.businessdomain.goods.infrastructure.factory.GoodsFactory;
import com.company.businessdomain.goods.domain.repository.GoodsRepository;
import com.company.businessdomain.goods.domain.model.Goods;
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
    private GoodsService goodsService;

    @Override
    @Call(status = 30001, errorCode = "TRADE_GOODS_GET_EXCEPTION", errorMessage = "查询商品异常")
    public Goods acquireGoods(Long goodsId) {
        GoodsEntity goods = goodsService.getGoodsById(goodsId);

        if (goods != null) {
            return GoodsFactory.valueOf(goods);
        }

        throw ExceptionHelper.createNestedException(30002, "TRADE_GOODS_NOT_FOUND", "找不到商品");
    }
}
