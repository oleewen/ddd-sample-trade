package com.company.businessdomain.order.application.command;

import lombok.Data;

/**
 * 交易命令
 *
 * @author only
 * @since 2020-05-22
 */
@Data
public class OrderBuyCommand {
    /** 买家id */
    private Long buyerId;
    /** 商品id */
    private Long goodsId;
    /** 商品件数 */
    private Integer itemCount;

}
