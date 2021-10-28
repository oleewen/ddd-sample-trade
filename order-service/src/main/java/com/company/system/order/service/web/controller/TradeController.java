package com.company.system.order.service.web.controller;

import com.company.system.order.application.command.TradeBuyCommand;
import com.company.system.order.application.service.TradeApplicationService;
import com.company.system.order.application.result.TradeBuyResult;
import com.company.system.order.service.web.reponse.TradeBuyResponse;
import com.company.system.order.service.web.request.TradeBuyRequest;
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
@RequestMapping("/api/trade")
public class TradeController {

    /** 订单流程 */
    @Resource
    private TradeApplicationService tradeApplicationService;

    @PostMapping("/buy")
    @ResponseBody
    @Call(elapsed = 1200, sample = 10000)
    public TradeBuyResponse buy(@Valid @RequestBody TradeBuyRequest trade, BindingResult bindingResult) {
        /** 处理参数验证错误 */
        if (bindingResult.hasErrors()) {
            // TODO
            return TradeBuyResponse.empty();
        }

        /** 请求转命令 */
        TradeBuyCommand buyCommand = trade.asCommand();

        /** 交易下单 */
        TradeBuyResult result = tradeApplicationService.doBuy(buyCommand);

        /** 输出转换 */
        return TradeBuyResponse.valueOf(result);
    }

}
