package com.thai.finance.api.finance.api.mapper;

import com.thai.finance.api.finance.api.dtos.categoryDTO.ResponseCategoryDTO;
import com.thai.finance.api.finance.api.dtos.supplierDTO.CreateSupplierDTO;
import com.thai.finance.api.finance.api.dtos.supplierDTO.ResponseSupplierDTO;
import com.thai.finance.api.finance.api.entities.Category;
import com.thai.finance.api.finance.api.entities.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {
    public ResponseSupplierDTO EntityResponseToDTO (Supplier supplierEntity) {

        ResponseSupplierDTO entityConverted =  new ResponseSupplierDTO(supplierEntity.getId(),supplierEntity.getNameSupplier());
        return  entityConverted;

    };
    public  Supplier CreateDtoToEntity(CreateSupplierDTO createSupplierDTO) {
         Supplier dtoSupplierConvertedToEntity =  new Supplier(null, createSupplierDTO.name());
         return dtoSupplierConvertedToEntity;
    }
}
