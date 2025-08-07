package com.kylin.order.service.factory;

import com.kylin.order.api.module.request.OrderBuyRequest;
import com.kylin.order.application.command.OrderBuyCommand;
import org.springframework.ext.common.helper.BeanHelper;

public class OrderCommandFactory {
    public static OrderBuyCommand asCommand(OrderBuyRequest buyRequest) {
        OrderBuyCommand command = BeanHelper.copyProperties(new OrderBuyCommand(), buyRequest);
        return command;
    }
}
