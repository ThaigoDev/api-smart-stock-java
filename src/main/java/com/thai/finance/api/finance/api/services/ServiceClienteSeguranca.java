package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.ClienteSegurancaDTO.ClienteSegurancaRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.ClienteSegurancaDTO.ClienteSegurancaRespostaDTO;
import com.thai.finance.api.finance.api.mapper.MapperClienteSeguranca;
import com.thai.finance.api.finance.api.respository.RepositoryClienteSeguranca;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceClienteSeguranca {
    private final MapperClienteSeguranca mapper;
    private final RepositoryClienteSeguranca repositoryClienteSeguranca;

    public ClienteSegurancaRespostaDTO salvar(ClienteSegurancaRequisicaoDTO clienteSegurancaRequisicaoDTO) {
        return  mapper.paraDTO(repositoryClienteSeguranca.save(mapper.paraEntidade(clienteSegurancaRequisicaoDTO))) ;
    }

    public ClienteSegurancaRespostaDTO obterPorId(String clientId) {
      return mapper.paraDTO(repositoryClienteSeguranca
              .findById(UUID.fromString(clientId))
              .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Cliente não encontrado")));
    }
}
