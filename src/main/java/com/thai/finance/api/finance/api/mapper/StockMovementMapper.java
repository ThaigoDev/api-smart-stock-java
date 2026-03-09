package com.thai.finance.api.finance.api.mapper;

import com.thai.finance.api.finance.api.domain.dtos.stockMovementDTO.CreateStockMovementDTO;
import com.thai.finance.api.finance.api.domain.dtos.stockMovementDTO.ResponseMovementStockDTO;
import com.thai.finance.api.finance.api.domain.entities.MovimentacaoEstoque;
import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {
    public ResponseMovementStockDTO entityStockMovementToResponseStockMovementDTO(MovimentacaoEstoque stockMovement) {
       return new ResponseMovementStockDTO(stockMovement.getId(), stockMovement.getProduto().getId(), stockMovement.getType(), stockMovement.getQuantityMovement());

    }
    public MovimentacaoEstoque dtoCreateMovementStocktoEntityStockMovement(CreateStockMovementDTO createStockMovementDTO) {
        return  new MovimentacaoEstoque(null, null, createStockMovementDTO.type(),createStockMovementDTO.quantity());
    }
}
