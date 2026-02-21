package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.dtos.supplierDTO.CreateSupplierDTO;
import com.thai.finance.api.finance.api.dtos.supplierDTO.ResponseSupplierDTO;
import com.thai.finance.api.finance.api.dtos.supplierDTO.UpdateSupplierDTO;
import com.thai.finance.api.finance.api.mapper.SupplierMapper;
import com.thai.finance.api.finance.api.respository.SupplierRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

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
        return  supplierRepository.findAll().stream().map(supplierMapper::EntityResponseToDTO).toList();

    };
    public void deleteSupplierById(UUID supplierId) {
       var supplierExist =    supplierRepository.findById(supplierId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));
        supplierRepository.deleteById(supplierExist.getId());
    }

    public void updateSupplier(UUID supplierId, UpdateSupplierDTO updateSupplierDTO) {
        var supplierExist  = supplierRepository.findById(supplierId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Supplier not found"));
        supplierExist.setNameSupplier(updateSupplierDTO.name());
        supplierRepository.save(supplierExist);

    }
}
