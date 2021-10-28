package com.company.system.inventory.domain.model;

import com.company.system.goods.domain.model.GoodsId;

/**
 * 库存（值对象）
 * <pre>
 * 1、计算公式：
 * 总库存 = 可用库存 + 占用库存 + 已售库存
 *
 * 2、锁定库存
 * 总库存 = ( 可用库存 - 锁定库存 ) + ( 占用库存 + 锁定库存 ) + 已售库存
 *
 * 3、扣减库存
 * 总库存 = 可用库存 + ( 占用库存 - 售卖库存 ) + ( 已售库存 + 售卖库存 )
 *
 * 4、回退库存
 * 总库存 = ( 可用库存 + 回退库存 ) + 占用库存 + ( 已售库存 - 回退库存 )
 * </pre>
 *
 * @author only
 * @since 2020-05-27
 */
public class Inventory {
    /** 商品id */
    private GoodsId goodsId;
    // /** 库存类型：普通商品库存，活动库存 */
    // private InventoryType type;
    /** 可用库存 */
    private Long available;
    /** 占用库存 */
    private Long locked;
    /** 已售库存 */
    private Long sold;
    /** 锁定库存 */
    private Integer lock;

    private Inventory(GoodsId goodsId) {
        this.goodsId = goodsId;
    }

    /**
     * 创建库存，并锁定
     *
     * @param goodsId 商品id
     * @param lock    锁定库存数量
     *
     * @return 库存对象
     */
    public static Inventory createLock(GoodsId goodsId, Integer lock) {
        Inventory inventory = new Inventory(goodsId);
        inventory.lock = lock;
        return inventory;
    }
}
