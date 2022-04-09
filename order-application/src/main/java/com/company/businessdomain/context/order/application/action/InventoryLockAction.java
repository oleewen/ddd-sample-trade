package com.company.businessdomain.context.order.application.action;

import com.company.businessdomain.context.inventory.domain.service.InventoryDomainService;
import com.company.businessdomain.context.order.domain.model.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 锁库存
 *
 * @author only
 * @date 2020-05-22
 */
@Component
public class InventoryLockAction {
    /** 优惠计算 */
    @Resource
    private InventoryDomainService inventoryDomainService;

    public boolean lock(Order order) {
        /** 锁定库存 */
        return inventoryDomainService.lock(order.getGoodsId(), order.getItemCount());
    }
}
