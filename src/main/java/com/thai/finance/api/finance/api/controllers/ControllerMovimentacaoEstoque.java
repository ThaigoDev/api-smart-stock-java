package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO.MovimentacaoEstoqueRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO.MovimentacaoEstoqueRespostaDTO;
import com.thai.finance.api.finance.api.services.ServiceMovimentacaoEstoque;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("movimentacoes-estoque")
@RequiredArgsConstructor
public class ControllerMovimentacaoEstoque {
    private final ServiceMovimentacaoEstoque serviceMovimentacaoEstoque;


    @PostMapping
    public ResponseEntity<MovimentacaoEstoqueRespostaDTO> salvarMovimentacaoEstoque(@RequestBody @Valid MovimentacaoEstoqueRequisicaoDTO movimentacaoEstoqueRequisicaoDTO) {

        var estoqueCriado = serviceMovimentacaoEstoque.salvar(movimentacaoEstoqueRequisicaoDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(estoqueCriado.id())
                .toUri();

         return  ResponseEntity.created(location).body(estoqueCriado);
    }
    @GetMapping
    public ResponseEntity<List<MovimentacaoEstoqueRespostaDTO>> obterMovimentacoesEstoques() {
        return  ResponseEntity.ok().body(serviceMovimentacaoEstoque.obter());
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> removerMovimentacaoEstoque(@PathVariable("id") UUID movimentacaoEstoque_id) {
        serviceMovimentacaoEstoque.remover(movimentacaoEstoque_id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("{id}")
    public ResponseEntity<MovimentacaoEstoqueRespostaDTO> atualizarMovimentacaoEstoque(@PathVariable("id") UUID movimentacaoEstoque_id, @RequestBody @Valid MovimentacaoEstoqueRequisicaoDTO movimentacaoEstoqueRequisicaoDTO) {
        return ResponseEntity.ok().body(serviceMovimentacaoEstoque.atualizar(movimentacaoEstoque_id, movimentacaoEstoqueRequisicaoDTO));
    }
 }
