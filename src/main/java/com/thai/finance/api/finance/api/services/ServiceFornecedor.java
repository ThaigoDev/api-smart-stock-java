package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.FornecedorDTO.FornecedorRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.FornecedorDTO.FornecedorRespostaDTO;
import com.thai.finance.api.finance.api.mapper.MapperFornecedor;
import com.thai.finance.api.finance.api.respository.RepositoryFornecedor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceFornecedor {
    private final RepositoryFornecedor repositoryFornecedor;
    private final MapperFornecedor mapper;


    public FornecedorRespostaDTO salvar(FornecedorRequisicaoDTO fornecedorRequisicaoDTO) {

        return  mapper.paraDTO(repositoryFornecedor.save(mapper.paraEntidade(fornecedorRequisicaoDTO)));

    }
    public List<FornecedorRespostaDTO> obter() {
        return repositoryFornecedor.findAll().stream().map(mapper::paraDTO).toList();

    }

    public void remover(UUID fornecedor_id) {
        var fornecedorEncontrado = repositoryFornecedor.findById(fornecedor_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor não encontrado"));
        repositoryFornecedor.deleteById(fornecedorEncontrado.getId());
    }

    public void atualizar(UUID fornecedor_id, FornecedorRequisicaoDTO fornecedorRequisicaoDTO) {
        var fornecedorEncontrado = repositoryFornecedor.findById(fornecedor_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor não encontrado"));
        fornecedorEncontrado.setNome(fornecedorRequisicaoDTO.nome());
        fornecedorEncontrado.setEmail(fornecedorRequisicaoDTO.email());
        fornecedorEncontrado.setTelefone(fornecedorRequisicaoDTO.telefone());
        repositoryFornecedor.save(fornecedorEncontrado);

    }
}
