package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.domain.dtos.stockMovementDTO.CreateStockMovementDTO;
import com.thai.finance.api.finance.api.domain.dtos.stockMovementDTO.ResponseMovementStockDTO;
import com.thai.finance.api.finance.api.domain.dtos.stockMovementDTO.UpdateMovementStockDTO;
import com.thai.finance.api.finance.api.services.StockMovementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/stockmovement")
public class StockMovementController {
    private final StockMovementService stockMovementService;

    public  StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }
    @PostMapping
    public ResponseEntity<ResponseMovementStockDTO> createStockMovement(@RequestBody @Valid CreateStockMovementDTO createStockMovementDTO) {
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(createStockMovementDTO.productId())
                .toUri();

         return  ResponseEntity.created(location).body( stockMovementService.createStockMovement(createStockMovementDTO));
    }
    @GetMapping
    public ResponseEntity<List<ResponseMovementStockDTO>> allStockMovements() {
        return  ResponseEntity.ok().body(stockMovementService.allStockMovements());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStockMovement(@PathVariable("id") UUID stockMovementId) {
        stockMovementService.deleteStockMovementById(stockMovementId);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<ResponseMovementStockDTO> putStockMovement(@PathVariable("id") UUID stockMovementId, @RequestBody @Valid UpdateMovementStockDTO updateMovementStockDTO) {
        return ResponseEntity.ok().body(stockMovementService.updateStockMovement(stockMovementId, updateMovementStockDTO));
    }
 }
