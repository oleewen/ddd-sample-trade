package com.company.businessdomain.context.order.api.module.response;

import com.company.businessdomain.context.common.enums.StatusCode;
import com.company.businessdomain.context.order.api.module.dto.OrderBuyDTO;
import org.springframework.ext.common.object.Status;
import org.springframework.ext.module.response.Response;
import lombok.Data;

@Data
public class OrderBuyResponse extends Response<OrderBuyDTO> {
    private static final OrderBuyResponse EMPTY = new OrderBuyResponse(StatusCode.PARAMETER_VALIDATE_ILLEGAL);

    public OrderBuyResponse(Status status) {
        super(status);
    }

    public static OrderBuyResponse empty() {
        return EMPTY;
    }

    public static OrderBuyResponse success() {
        return new OrderBuyResponse(StatusCode.SERVICE_RUN_SUCCESS);
    }

    public static OrderBuyResponse failure() {
        return new OrderBuyResponse(StatusCode.PARAMETER_VALIDATE_ILLEGAL);
    }

    public static OrderBuyResponse error() {
        return new OrderBuyResponse(StatusCode.SERVICE_RUN_ERROR);
    }
}
