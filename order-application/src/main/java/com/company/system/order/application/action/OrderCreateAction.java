package com.company.system.order.application.action;

import com.company.system.goods.domain.facade.ItemQueryFacade;
import com.company.system.goods.domain.model.Goods;
import com.company.system.order.application.command.TradeBuyCommand;
import com.company.system.order.domain.model.Order;
import com.company.system.order.domain.service.OrderDomainService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 订单创建
 *
 * @author only
 * @since 2020-05-22
 */
@Component
public class OrderCreateAction {
    /** 商品查询 */
    @Resource
    private ItemQueryFacade itemQueryFacade;
    /** 订单服务 */
    @Resource
    private OrderDomainService orderDomainService;

    public Order create(TradeBuyCommand buy) {
        /** 查询商品 */
        Goods goods = itemQueryFacade.requireGoodsById(buy.getGoodsId());

        /** 创建订单 */
        return orderDomainService.create(buy.getBuyerId(), goods, buy.getItemCount());
    }
}
