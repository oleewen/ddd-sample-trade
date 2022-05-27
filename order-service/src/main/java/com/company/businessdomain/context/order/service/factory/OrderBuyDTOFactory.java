package com.company.businessdomain.context.order.service.factory;

import com.company.businessdomain.context.order.api.module.dto.OrderBuyDTO;
import com.company.businessdomain.context.order.application.result.OrderBuyResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author only
 * @date 2022-05-27
 */
@Mapper
public interface OrderBuyDTOFactory {
    OrderBuyDTOFactory INSTANCE = Mappers.getMapper(OrderBuyDTOFactory.class);

    OrderBuyDTO toDTO(OrderBuyResult result);
}
