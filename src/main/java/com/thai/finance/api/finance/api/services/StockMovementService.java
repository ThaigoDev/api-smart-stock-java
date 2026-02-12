package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.dtos.stockMovementDTO.CreateStockMovementDTO;
import com.thai.finance.api.finance.api.dtos.stockMovementDTO.ResponseMovementStockDTO;
import com.thai.finance.api.finance.api.dtos.stockMovementDTO.UpdateMovementStockDTO;
import com.thai.finance.api.finance.api.entities.Product;
import com.thai.finance.api.finance.api.entities.Stock_Movement;
import com.thai.finance.api.finance.api.mapper.StockMovementMapper;
import com.thai.finance.api.finance.api.respository.ProductRepository;
import com.thai.finance.api.finance.api.respository.StockMovementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class StockMovementService {
    private final StockMovementRepository stockMovementRepository;
    private final StockMovementMapper stockMovementMapper;
    private final ProductRepository productRepository;

    public StockMovementService(StockMovementRepository stockMovementRepository, StockMovementMapper stockMovementMapper, ProductRepository productRepository) {
        this.stockMovementRepository = stockMovementRepository;
        this.stockMovementMapper = stockMovementMapper;
        this.productRepository = productRepository;
    }

    public ResponseMovementStockDTO createStockMovement(CreateStockMovementDTO createStockMovementDTO) {
        if(createStockMovementDTO.productId() ==null) {
            throw new IllegalArgumentException("priductId is obrigatory");
        }
        Product productFinded =  productRepository.findById(createStockMovementDTO.productId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Product Not Found"));
        Stock_Movement  stockMovementConverted = stockMovementMapper.dtoCreateMovementStocktoEntityStockMovement(createStockMovementDTO);
        stockMovementConverted.setProduct(productFinded);
        var stockMovementSaved=    stockMovementMapper.entityStockMovementToResponseStockMovementDTO( stockMovementRepository.save(stockMovementConverted)) ;
       return stockMovementSaved;
    }

    public List<ResponseMovementStockDTO> allStockMovements() {
      return    stockMovementRepository.findAll().stream().map(stockMovement -> stockMovementMapper.entityStockMovementToResponseStockMovementDTO(stockMovement)).toList();
    }

    public void deleteStockMovementById(UUID stockMovementId) {
        stockMovementRepository.deleteById(stockMovementId);
    }

    public ResponseMovementStockDTO updateStockMovement( UUID id, UpdateMovementStockDTO updateMovementStockDTO) {
        Stock_Movement  stockMovementExist = stockMovementRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Stock Movement not found"));
        Product productFined =  productRepository.findById(updateMovementStockDTO.productId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prodcut Not found"));
        stockMovementExist.setProduct(productFined);
        stockMovementExist.setType(updateMovementStockDTO.type());
        stockMovementExist.setQuantityMovement(updateMovementStockDTO.quantity());
        stockMovementExist.setUpdateAt(Instant.now());

        var stockMovementCreated =  stockMovementMapper.entityStockMovementToResponseStockMovementDTO(stockMovementRepository.save(stockMovementExist));
       return stockMovementCreated;
    }
}
