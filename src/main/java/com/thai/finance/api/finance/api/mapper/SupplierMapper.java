package com.thai.finance.api.finance.api.mapper;

import com.thai.finance.api.finance.api.domain.dtos.supplierDTO.CreateSupplierDTO;
import com.thai.finance.api.finance.api.domain.dtos.supplierDTO.ResponseSupplierDTO;
import com.thai.finance.api.finance.api.domain.entities.Fornecedor;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {
    public ResponseSupplierDTO EntityResponseToDTO (Fornecedor fornecedorEntity) {

        ResponseSupplierDTO entityConverted =  new ResponseSupplierDTO(fornecedorEntity.getId(), fornecedorEntity.getNameSupplier());
        return  entityConverted;

    };
    public Fornecedor CreateDtoToEntity(CreateSupplierDTO createSupplierDTO) {
         Fornecedor dtoFornecedorConvertedToEntity =  new Fornecedor( createSupplierDTO.name());
         return dtoFornecedorConvertedToEntity;
    }
}
