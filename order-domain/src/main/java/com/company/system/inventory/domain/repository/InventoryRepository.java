package com.company.system.inventory.domain.repository;

import com.company.system.inventory.domain.model.Inventory;

/**
 * 库存资源库
 *
 * @author only
 * @since 2020-05-27
 */
public interface InventoryRepository {
    /**
     * 锁定库存
     *
     * @param inventory 库存对象
     *
     * @return 是否锁定成果
     */
    boolean lock(Inventory inventory);
}
