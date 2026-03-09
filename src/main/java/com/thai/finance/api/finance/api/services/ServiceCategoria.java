package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.CategoriaDTO.CategoriaRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.CategoriaDTO.CategoriaRespostaDTO;
import com.thai.finance.api.finance.api.mapper.MapperCategory;
import com.thai.finance.api.finance.api.respository.RepositoryCategoria;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceCategoria {
    private final RepositoryCategoria repositoryCategoria;
    private final MapperCategory mapper;


    public CategoriaRespostaDTO createCategory(CategoriaRequisicaoDTO categoriaRequisicaoDTO) {

        return mapper.entityToDTO(repositoryCategoria.save(mapper.dtoToEntity(categoriaRequisicaoDTO)));


    }

    public List<CategoriaRespostaDTO> getAllCategories() {
        return repositoryCategoria
                .findAll()
                .stream()
                .map(mapper::entityToDTO)
                .toList();
    }

    public void deleteCategoryById(UUID id) {
        var categoryExist = repositoryCategoria.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        repositoryCategoria.deleteById(categoryExist.getId());

    }
}
