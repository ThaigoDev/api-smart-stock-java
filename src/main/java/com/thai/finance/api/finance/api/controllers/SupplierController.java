package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.dtos.supplierDTO.CreateSupplierDTO;
import com.thai.finance.api.finance.api.dtos.supplierDTO.ResponseSupplierDTO;
import com.thai.finance.api.finance.api.services.SupplierService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/supplier")
public class SupplierController {
    private final SupplierService supplierService;

    public SupplierController (SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    public ResponseEntity<ResponseSupplierDTO> createSupplier(@RequestBody CreateSupplierDTO createSupplierDTO) {
        var createdSupplier  =  supplierService.createSupplier(createSupplierDTO);
        return ResponseEntity.ok(createdSupplier);
    }
    @GetMapping("/all")
    public ResponseEntity<List<ResponseSupplierDTO>>  getAllSuppliers () {
        var allSuppliers = supplierService.getAllSuppliers();
        return ResponseEntity.ok(allSuppliers);
    }
}
