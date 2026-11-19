package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.CategoriaDTO.CategoriaRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.CategoriaDTO.CategoriaRespostaDTO;
import com.thai.finance.api.finance.api.mapper.MapperCategoria;
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
    private final MapperCategoria mapper;


    public CategoriaRespostaDTO salvar(CategoriaRequisicaoDTO categoriaRequisicaoDTO) {

        return mapper.paraDTO(repositoryCategoria.save(mapper.paraEntidade(categoriaRequisicaoDTO)));


    }

    public List<CategoriaRespostaDTO> obter() {
        return repositoryCategoria
                .findAll()
                .stream()
                .map(mapper::paraDTO)
                .toList();
    }

    public CategoriaRespostaDTO atualizar(UUID categoria_id, CategoriaRequisicaoDTO categoriaRequisicaoDTO) {
        var categoriaEncontrada = repositoryCategoria.findById(categoria_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
        categoriaEncontrada.setNome(categoriaRequisicaoDTO.nome());
        categoriaEncontrada.setDescricao(categoriaRequisicaoDTO.descricao());
        return mapper.paraDTO(repositoryCategoria.save(categoriaEncontrada));
    }

    public void remover(UUID categoria_id) {
        var categoriaEncontrada = repositoryCategoria.findById(categoria_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
        repositoryCategoria.deleteById(categoriaEncontrada.getId());

    }
}
