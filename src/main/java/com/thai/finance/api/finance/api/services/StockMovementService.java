package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.stockMovementDTO.CreateStockMovementDTO;
import com.thai.finance.api.finance.api.domain.dtos.stockMovementDTO.ResponseMovementStockDTO;
import com.thai.finance.api.finance.api.domain.dtos.stockMovementDTO.UpdateMovementStockDTO;
import com.thai.finance.api.finance.api.domain.entities.Product;
import com.thai.finance.api.finance.api.domain.entities.Stock;
import com.thai.finance.api.finance.api.domain.entities.Stock_Movement;
import com.thai.finance.api.finance.api.domain.enums.MovementType;
import com.thai.finance.api.finance.api.mapper.StockMovementMapper;
import com.thai.finance.api.finance.api.respository.ProductRepository;
import com.thai.finance.api.finance.api.respository.StockMovementRepository;
import com.thai.finance.api.finance.api.respository.StockRespository;
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
    private final StockRespository stockRespository;

    public StockMovementService(StockMovementRepository stockMovementRepository, StockMovementMapper stockMovementMapper, ProductRepository productRepository, StockRespository stockRespository) {
        this.stockMovementRepository = stockMovementRepository;
        this.stockMovementMapper = stockMovementMapper;
        this.productRepository = productRepository;
        this.stockRespository = stockRespository;
    }

    public ResponseMovementStockDTO createStockMovement(CreateStockMovementDTO createStockMovementDTO) {
        if (createStockMovementDTO.productId() == null) {
            throw new IllegalArgumentException("priductId is obrigatory");
        }
        Product productFinded = productRepository.findById(createStockMovementDTO.productId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product Not Found"));
        Stock_Movement stockMovementConverted = stockMovementMapper.dtoCreateMovementStocktoEntityStockMovement(createStockMovementDTO);

        Stock stockFinded = stockRespository.findByProduct_Id(productFinded.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock Not Found"));

        if (createStockMovementDTO.type() == MovementType.OUT) {
            if (createStockMovementDTO.quantity() >= productFinded.getMinimum_stock()) {
                throw new IllegalArgumentException("not authorized the transaction");
            }

            productFinded.setInitialStock(productFinded.getInitialStock() - createStockMovementDTO.quantity());
            stockFinded.setQuantityProduct(stockFinded.getQuantityProduct() - createStockMovementDTO.quantity());
        } else {
            productFinded.setInitialStock(productFinded.getInitialStock() + createStockMovementDTO.quantity());
            stockFinded.setQuantityProduct(stockFinded.getQuantityProduct() + createStockMovementDTO.quantity());
        }
        productRepository.save(productFinded);
        stockRespository.save(stockFinded);

        stockMovementConverted.setProduct(productFinded);
        return stockMovementMapper.entityStockMovementToResponseStockMovementDTO(stockMovementRepository.save(stockMovementConverted));
    }

    public List<ResponseMovementStockDTO> allStockMovements() {
        return stockMovementRepository.findAll().stream().map(stockMovementMapper::entityStockMovementToResponseStockMovementDTO).toList();
    }

    public void deleteStockMovementById(UUID stockMovementId) {
        stockMovementRepository.deleteById(stockMovementId);
    }

    public ResponseMovementStockDTO updateStockMovement(UUID id, UpdateMovementStockDTO updateMovementStockDTO) {
        Stock_Movement stockMovementExist = stockMovementRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock Movement not found"));
        Product productFinded = productRepository.findById(updateMovementStockDTO.productId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prodcut Not found"));
        Stock stockFinded = stockRespository.findByProduct_Id(productFinded.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found"));

        if (updateMovementStockDTO.type() == MovementType.OUT) {
            if (updateMovementStockDTO.quantity() >= productFinded.getMinimum_stock()) {
                throw new IllegalArgumentException("not authorized the transaction");
            }
            productFinded.setInitialStock(productFinded.getInitialStock() - updateMovementStockDTO.quantity());
            stockFinded.setQuantityProduct(stockFinded.getQuantityProduct() - updateMovementStockDTO.quantity());
        } else {
            productFinded.setInitialStock(productFinded.getInitialStock() + updateMovementStockDTO.quantity());
            stockFinded.setQuantityProduct(stockFinded.getQuantityProduct() + updateMovementStockDTO.quantity());
        }


        stockMovementExist.setProduct(productFinded);
        stockMovementExist.setType(updateMovementStockDTO.type());
        stockMovementExist.setQuantityMovement(updateMovementStockDTO.quantity());
        stockMovementExist.setUpdateAt(Instant.now());

        productRepository.save(productFinded);
        stockRespository.save(stockFinded);

        return stockMovementMapper.entityStockMovementToResponseStockMovementDTO(stockMovementRepository.save(stockMovementExist));
    }
}
