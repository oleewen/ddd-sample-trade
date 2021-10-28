package com.company.system.inventory.domain.service;

import com.company.system.goods.domain.model.GoodsId;
import com.company.system.inventory.domain.model.Inventory;
import com.company.system.inventory.domain.repository.InventoryRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 库存领域服务
 *
 * @author only
 * @since 2020-05-27
 */
@Component
public class InventoryDomainService {
    /** 库存资源库 */
    @Resource
    private InventoryRepository inventoryRepository;

    public boolean lock(GoodsId goodsId, Integer lock) {
        /** 扣减商品库存 */
        Inventory inventory = Inventory.createLock(goodsId, lock);
        return inventoryRepository.lock(inventory);
    }
}
