package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.domain.dtos.supplierDTO.CreateSupplierDTO;
import com.thai.finance.api.finance.api.domain.dtos.supplierDTO.ResponseSupplierDTO;
import com.thai.finance.api.finance.api.domain.dtos.supplierDTO.UpdateSupplierDTO;
import com.thai.finance.api.finance.api.services.ServiceFornecedor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/supplier")
public class ControllerFornecedor {
    private final ServiceFornecedor serviceFornecedor;

    public ControllerFornecedor(ServiceFornecedor serviceFornecedor) {
        this.serviceFornecedor = serviceFornecedor;
    }

    @PostMapping
    public ResponseEntity<ResponseSupplierDTO> createSupplier(@RequestBody @Valid CreateSupplierDTO createSupplierDTO) {
        var createdSupplier  =  serviceFornecedor.createSupplier(createSupplierDTO);
        URI location  = ServletUriComponentsBuilder
                .fromCurrentRequest().path("{id}")
                .buildAndExpand(createdSupplier.id())
                .toUri();
        return ResponseEntity.created(location).body(createdSupplier);
    }
    @GetMapping("/all")
    public ResponseEntity<List<ResponseSupplierDTO>>  getAllSuppliers () {
        var allSuppliers = serviceFornecedor.getAllSuppliers();
        return ResponseEntity.ok(allSuppliers);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier (@PathVariable("id")UUID supplierId) {
         serviceFornecedor.deleteSupplierById(supplierId);
         return  ResponseEntity.noContent().build();

    }
    @PutMapping("/{id}")
    public ResponseEntity<Void> putSupplier(@PathVariable("id") UUID supplierId, @RequestBody @Valid UpdateSupplierDTO updateSupplierDTO) {
        serviceFornecedor.updateSupplier(supplierId,updateSupplierDTO);
        return ResponseEntity.noContent().build();
    };
}
