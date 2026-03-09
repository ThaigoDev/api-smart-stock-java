package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.supplierDTO.CreateSupplierDTO;
import com.thai.finance.api.finance.api.domain.dtos.supplierDTO.ResponseSupplierDTO;
import com.thai.finance.api.finance.api.domain.dtos.supplierDTO.UpdateSupplierDTO;
import com.thai.finance.api.finance.api.mapper.SupplierMapper;
import com.thai.finance.api.finance.api.respository.RepositoryFornecedor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ServiceFornecedor {
    private final RepositoryFornecedor repositoryFornecedor;
    private final SupplierMapper supplierMapper;

    public ServiceFornecedor(RepositoryFornecedor repositoryFornecedor, SupplierMapper supplierMapper) {
        this.repositoryFornecedor = repositoryFornecedor;
        this.supplierMapper = supplierMapper;
    }

    public ResponseSupplierDTO createSupplier(CreateSupplierDTO createSupplierDTO) {

        var supplierEntityConverted = supplierMapper.EntityResponseToDTO(repositoryFornecedor.save(supplierMapper.CreateDtoToEntity(createSupplierDTO)));
        return supplierEntityConverted;
    }

    ;

    public List<ResponseSupplierDTO> getAllSuppliers() {
        return repositoryFornecedor.findAll().stream().map(supplierMapper::EntityResponseToDTO).toList();

    }

    ;

    public void deleteSupplierById(UUID supplierId) {
        var supplierExist = repositoryFornecedor.findById(supplierId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));
        repositoryFornecedor.deleteById(supplierExist.getId());
    }

    public void updateSupplier(UUID supplierId, UpdateSupplierDTO updateSupplierDTO) {
        var supplierExist = repositoryFornecedor.findById(supplierId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));
        supplierExist.setNameSupplier(updateSupplierDTO.name());
        repositoryFornecedor.save(supplierExist);

    }
}
