package com.thai.finance.api.finance.api.mapper;

import com.thai.finance.api.finance.api.domain.dtos.stockDTO.CreateStockDTO;
import com.thai.finance.api.finance.api.domain.dtos.stockDTO.ResponseStockDTO;
import com.thai.finance.api.finance.api.domain.entities.Estoque;
import org.springframework.stereotype.Component;

@Component
public class StockMapper {
    public ResponseStockDTO entityToResponseDTO(Estoque estoque) {
        ResponseStockDTO  entityConvertedToDto = new ResponseStockDTO(estoque.getId(), estoque.getProduto().getId(), estoque.getProduto().getNameProduct() , estoque.getQuantityProduct());
        return entityConvertedToDto;
    };
    public Estoque createStockDTOtoEntity (CreateStockDTO createStockDTO) {
        Estoque dtoConvertedToEntity = new Estoque(null,createStockDTO.produto() ,createStockDTO.quantityProduct());
        return dtoConvertedToEntity;
    };
}

