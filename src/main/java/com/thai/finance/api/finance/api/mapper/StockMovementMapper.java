package com.thai.finance.api.finance.api.mapper;

import com.thai.finance.api.finance.api.dtos.stockMovementDTO.CreateStockMovementDTO;
import com.thai.finance.api.finance.api.dtos.stockMovementDTO.ResponseMovementStockDTO;
import com.thai.finance.api.finance.api.entities.Stock_Movement;
import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {
    public ResponseMovementStockDTO entityStockMovementToResponseStockMovementDTO(Stock_Movement stockMovement) {
       return new ResponseMovementStockDTO(stockMovement.getId(), stockMovement.getProduct().getId(), stockMovement.getType(), stockMovement.getQuantityMovement());

    }
    public  Stock_Movement dtoCreateMovementStocktoEntityStockMovement(CreateStockMovementDTO createStockMovementDTO) {
        return  new Stock_Movement(null, null, createStockMovementDTO.type(),createStockMovementDTO.quantity());
    }
}
