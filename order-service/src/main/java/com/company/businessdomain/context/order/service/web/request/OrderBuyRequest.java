package com.company.businessdomain.context.order.service.web.request;

import com.company.businessdomain.context.order.application.command.OrderBuyCommand;
import lombok.Data;
import org.springframework.ext.common.helper.BeanHelper;

/**
 * @author only
 * @since 2020-05-22
 */
@Data
public class OrderBuyRequest {
    /** 买家id */
    private Long buyerId;
    /** 商品id */
    private Long goodsId;
    /** 商品件数 */
    private Long itemCount;

    public OrderBuyCommand asCommand() {
        OrderBuyCommand command = BeanHelper.copyProperties(new OrderBuyCommand(), this);
        return command;
    }
}
