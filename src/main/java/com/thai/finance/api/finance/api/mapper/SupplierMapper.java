package com.thai.finance.api.finance.api.mapper;

import com.thai.finance.api.finance.api.domain.dtos.supplierDTO.CreateSupplierDTO;
import com.thai.finance.api.finance.api.domain.dtos.supplierDTO.ResponseSupplierDTO;
import com.thai.finance.api.finance.api.domain.entities.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {
    public ResponseSupplierDTO EntityResponseToDTO (Supplier supplierEntity) {

        ResponseSupplierDTO entityConverted =  new ResponseSupplierDTO(supplierEntity.getId(),supplierEntity.getNameSupplier());
        return  entityConverted;

    };
    public  Supplier CreateDtoToEntity(CreateSupplierDTO createSupplierDTO) {
         Supplier dtoSupplierConvertedToEntity =  new Supplier( createSupplierDTO.name());
         return dtoSupplierConvertedToEntity;
    }
}
