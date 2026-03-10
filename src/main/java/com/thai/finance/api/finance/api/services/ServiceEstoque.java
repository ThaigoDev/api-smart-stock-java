package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.EstoqueDTO.EstoqueRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.EstoqueDTO.EstoqueRespostaDTO;
import com.thai.finance.api.finance.api.mapper.StockMapper;
import com.thai.finance.api.finance.api.respository.RepositoryEstoque;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceEstoque {
    private final RepositoryEstoque stockRepository;
    private final StockMapper stockMapper;

    public EstoqueRespostaDTO salvar(EstoqueRequisicaoDTO estoqueRequisicaoDTO) {
        return stockMapper.entityToResponseDTO(stockRepository.save(stockMapper.createStockDTOtoEntity(estoqueRequisicaoDTO)));
    }

    public List<EstoqueRespostaDTO> obter() {
        return stockRepository.findAll().stream().map(stockMapper::entityToResponseDTO).toList();
    }

    public EstoqueRespostaDTO obterPorId(UUID estoque_id) {
        return stockMapper.entityToResponseDTO(stockRepository.findById(estoque_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque não encontrado")));
    }
}
