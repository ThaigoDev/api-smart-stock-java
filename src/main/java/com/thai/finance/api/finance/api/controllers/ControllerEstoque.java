package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.domain.dtos.stockDTO.CreateStockDTO;
import com.thai.finance.api.finance.api.domain.dtos.stockDTO.ResponseStockDTO;
import com.thai.finance.api.finance.api.services.ServiceEstoque;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/stock")
public class ControllerEstoque {
    private ServiceEstoque serviceEstoque;
    public ControllerEstoque(ServiceEstoque serviceEstoque) {
        this.serviceEstoque = serviceEstoque;
    }
    @PostMapping
    public ResponseEntity<ResponseStockDTO> createStock(@RequestBody @Valid CreateStockDTO createStockDTO) {
      var stockCreated = serviceEstoque.createStock(createStockDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(stockCreated.id())
                .toUri();

      return ResponseEntity.created(location).body(stockCreated);
    };
    @GetMapping
    public ResponseEntity<List<ResponseStockDTO>> getAllStocks () {
        var allStocks = serviceEstoque.getAllStocks();
        return ResponseEntity.ok(allStocks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseStockDTO> getStockById(@PathVariable("id") UUID stockId) {
        var stock = serviceEstoque.getStockById(stockId);
        return  ResponseEntity.ok(stock);
    };
}
