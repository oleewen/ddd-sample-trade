package com.company.department.business.goods.resource.dal;

import com.company.department.business.goods.domain.model.Goods;
import com.company.department.business.goods.domain.repository.GoodsRepository;
import com.company.department.business.goods.resource.call.GoodsCall;
import com.company.department.business.goods.resource.entity.GoodsEntity;
import com.company.department.business.goods.resource.factory.GoodsFactory;
import org.springframework.ext.common.aspect.Call;
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
    @Call
    public Goods acquireGoods(Long goodsId) {
        GoodsEntity goods = goodsCall.getGoodsById(goodsId);

        return GoodsFactory.valueOf(goods);
    }
}
