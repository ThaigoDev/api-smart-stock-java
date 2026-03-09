package com.thai.finance.api.finance.api.mapper;

import com.thai.finance.api.finance.api.domain.dtos.ProdutoDTO.ProdutoRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.ProdutoDTO.ProdutoRespostaDTO;
import com.thai.finance.api.finance.api.domain.entities.Produto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MapperProduct {

 @Mapping(target = "categoryId.id" , source = "categoryId")
 @Mapping(target = "supplier.id" , source = "supplierId")
 @Mapping(target = "stock.id" , source = "stock")
 Produto dtoToEntity(ProdutoRequisicaoDTO produtoRequisicaoDTO);

 @Mapping(target = "categoryId", source = "categoryId.id")
 @Mapping(target = "supplierId", source = "supplier.id")
 @Mapping(target = "stock", source = "stock.id")
 ProdutoRespostaDTO entityToDTO(Produto produto);
}
