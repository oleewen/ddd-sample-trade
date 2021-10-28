package com.company.system.goods.resource.entity;

import lombok.Data;

/**
 * 商品数据对象
 *
 * @author only
 * @date 2020-05-22
 */
@Data
public class GoodsEntity {
    /** 商品id */
    private Long id;
    /** 商品标题 */
    private String title;
    /** 商品价格 */
    private int price;
}
