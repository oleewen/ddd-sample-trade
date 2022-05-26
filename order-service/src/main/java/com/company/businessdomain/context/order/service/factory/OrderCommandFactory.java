package com.company.businessdomain.context.order.service.factory;

import com.company.businessdomain.context.order.api.module.request.OrderBuyRequest;
import com.company.businessdomain.context.order.application.command.OrderBuyCommand;
import org.springframework.ext.common.helper.BeanHelper;

public class OrderCommandFactory {
    public static OrderBuyCommand asCommand(OrderBuyRequest buyRequest) {
        OrderBuyCommand command = BeanHelper.copyProperties(new OrderBuyCommand(), buyRequest);
        return command;
    }
}
