package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.domain.dtos.EstoqueDTO.EstoqueRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.EstoqueDTO.EstoqueRespostaDTO;
import com.thai.finance.api.finance.api.services.ServiceEstoque;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/stock")
@RequiredArgsConstructor
public class ControllerEstoque {
    private ServiceEstoque serviceEstoque;

    @PostMapping
    public ResponseEntity<EstoqueRespostaDTO> salvarEstoque(@RequestBody @Valid EstoqueRequisicaoDTO estoqueRequisicaoDTO) {
      var estoqueCriado = serviceEstoque.salvar(estoqueRequisicaoDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(estoqueCriado.id())
                .toUri();

      return ResponseEntity.created(location).body(estoqueCriado);
    };

    @GetMapping
    public ResponseEntity<List<EstoqueRespostaDTO>> obterEstoques () {
        var estoques = serviceEstoque.obter();
        return ResponseEntity.ok(estoques);
    }

    @GetMapping("{id}")
    public ResponseEntity<EstoqueRespostaDTO> obterEstoquePorId(@PathVariable("id") UUID estoque_id) {
        var estoque = serviceEstoque.obterPorId(estoque_id);
        return  ResponseEntity.ok().body(estoque);
    };
}
