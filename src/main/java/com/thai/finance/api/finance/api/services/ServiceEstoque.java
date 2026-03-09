package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.stockDTO.CreateStockDTO;
import com.thai.finance.api.finance.api.domain.dtos.stockDTO.ResponseStockDTO;
import com.thai.finance.api.finance.api.mapper.StockMapper;
import com.thai.finance.api.finance.api.respository.RepositoryEstoque;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceEstoque {
    private final RepositoryEstoque stockRepository;
    private final StockMapper stockMapper;

    public ResponseStockDTO createStock(CreateStockDTO createStockDTO) {
        return stockMapper.entityToResponseDTO(stockRepository.save(stockMapper.createStockDTOtoEntity(createStockDTO)));
    }

    public List<ResponseStockDTO> getAllStocks() {
        return stockRepository.findAll().stream().map(stockMapper::entityToResponseDTO).toList();
    }

    public ResponseStockDTO getStockById(UUID stockId) {
        return stockMapper.entityToResponseDTO(stockRepository.findById(stockId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not Found")));
    }
}
