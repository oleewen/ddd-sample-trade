package com.company.businessdomain.context.order.service.web.controller;

import com.company.businessdomain.context.order.application.command.OrderBuyCommand;
import com.company.businessdomain.context.order.application.result.OrderBuyResult;
import com.company.businessdomain.context.order.application.service.OrderApplicationService;
import com.company.businessdomain.context.order.service.web.reponse.OrderBuyResponse;
import com.company.businessdomain.context.order.service.web.request.OrderBuyRequest;
import org.springframework.ext.common.aspect.Call;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 交易控制器
 *
 * @author only
 * @date 2020-05-22
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    /** 订单流程 */
    @Resource
    private OrderApplicationService orderApplicationService;

    @PostMapping("/buy")
    @ResponseBody
    @Call(elapsed = 1200, sample = 10000)
    public OrderBuyResponse buy(@Valid @RequestBody OrderBuyRequest order, BindingResult bindingResult) {
        /** 处理参数验证错误 */
        if (bindingResult.hasErrors()) {
            // TODO
            return OrderBuyResponse.empty();
        }

        /** 请求转命令 */
        OrderBuyCommand buyCommand = order.asCommand();

        /** 交易下单 */
        OrderBuyResult result = orderApplicationService.doBuy(buyCommand);

        /** 输出转换 */
        return OrderBuyResponse.valueOf(result);
    }

}
