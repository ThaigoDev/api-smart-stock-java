package com.thai.finance.api.finance.api.mapper;

import com.thai.finance.api.finance.api.domain.dtos.stockMovementDTO.CreateStockMovementDTO;
import com.thai.finance.api.finance.api.domain.dtos.stockMovementDTO.ResponseMovementStockDTO;
import com.thai.finance.api.finance.api.domain.entities.MovimentacaoEstoque;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MapperStockMovement {

    MovimentacaoEstoque dtoToEntity(CreateStockMovementDTO createStockMovementDTO);
    ResponseMovementStockDTO entityToDto(MovimentacaoEstoque stockMovement);

}
