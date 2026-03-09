package com.thai.finance.api.finance.api.mapper;

import com.thai.finance.api.finance.api.domain.dtos.CategoriaDTO.CategoriaRespostaDTO;
import com.thai.finance.api.finance.api.domain.entities.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoriaRespostaDTO EntityResponseToDTO (Categoria categoria) {

        CategoriaRespostaDTO entityConverted =  new CategoriaRespostaDTO(categoria.getId(), categoria.getName());
        return  entityConverted;

    };
}
