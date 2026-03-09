package com.thai.finance.api.finance.api.mapper;

import com.thai.finance.api.finance.api.domain.dtos.CategoriaDTO.CategoriaRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.CategoriaDTO.CategoriaRespostaDTO;
import com.thai.finance.api.finance.api.domain.entities.Categoria;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MapperCategory {
    Categoria dtoToEntity (CategoriaRequisicaoDTO categoriaRequisicaoDTO);
    CategoriaRespostaDTO entityToDTO (Categoria categoria);
}
