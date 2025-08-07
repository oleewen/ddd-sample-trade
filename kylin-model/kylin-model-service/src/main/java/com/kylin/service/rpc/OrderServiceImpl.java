package com.kylin.order.service.rpc;

import com.kylin.order.api.OrderService;
import com.kylin.order.api.module.request.OrderBuyRequest;
import com.kylin.order.api.module.response.OrderBuyResponse;
import com.kylin.order.application.command.OrderBuyCommand;
import com.kylin.order.application.result.OrderBuyResult;
import com.kylin.order.application.service.OrderApplicationService;
import com.kylin.order.service.factory.OrderCommandFactory;
import com.kylin.order.service.factory.OrderResultFactory;
import org.springframework.ext.common.aspect.Call;

/**
 * 服务实现样例
 *
 * @author only
 * @since 2020-05-22
 */
public class OrderServiceImpl implements OrderService {
    private OrderApplicationService orderApplicationService;

    @Override
    @Call(elapsed = 1200, sample = 10000)
    public OrderBuyResponse buy(OrderBuyRequest buyRequest) {
        /** 处理参数验证错误 */
        if (buyRequest.validate()) {
            return OrderBuyResponse.empty();
        }

        /** 请求转命令 */
        OrderBuyCommand buyCommand = OrderCommandFactory.asCommand(buyRequest);

        /** 交易下单 */
        OrderBuyResult buyResult = orderApplicationService.doBuy(buyCommand);

        /** 输出转换 */
        return OrderResultFactory.asResponse(buyResult);
    }
}

