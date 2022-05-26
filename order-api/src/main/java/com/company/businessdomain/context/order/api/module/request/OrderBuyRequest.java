package com.company.businessdomain.context.order.api.module.request;

import lombok.Data;
import org.springframework.ext.module.request.Request;

@Data
public class OrderBuyRequest extends Request {
    /** 买家id */
    private Long buyerId;
    /** 商品id */
    private Long goodsId;
    /** 商品件数 */
    private Long itemCount;

    public boolean validate() {
        return false;
    }
}
