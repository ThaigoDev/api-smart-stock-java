package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.dtos.supplierDTO.CreateSupplierDTO;
import com.thai.finance.api.finance.api.dtos.supplierDTO.ResponseSupplierDTO;
import com.thai.finance.api.finance.api.mapper.SupplierMapper;
import com.thai.finance.api.finance.api.respository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public SupplierService( SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    public ResponseSupplierDTO createSupplier(CreateSupplierDTO createSupplierDTO) {

         var supplierEntityConverted =  supplierMapper.EntityResponseToDTO(supplierRepository.save(supplierMapper.CreateDtoToEntity(createSupplierDTO)));
        return supplierEntityConverted;
    };

    public List<ResponseSupplierDTO> getAllSuppliers() {
        return  supplierRepository.findAll().stream().map(supplier -> supplierMapper.EntityResponseToDTO(supplier)).toList();

    };
}
