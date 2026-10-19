package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.domain.dtos.FornecedorDTO.FornecedorRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.FornecedorDTO.FornecedorRespostaDTO;
import com.thai.finance.api.finance.api.services.ServiceFornecedor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("fornecedores")
@RequiredArgsConstructor
public class ControllerFornecedor {
    private final ServiceFornecedor serviceFornecedor;

    @PostMapping
    public ResponseEntity<FornecedorRespostaDTO> salvarFornecedor(@RequestBody @Valid FornecedorRequisicaoDTO fornecedorRequisicaoDTO) {
        var fornecedorCriado  =  serviceFornecedor.salvar(fornecedorRequisicaoDTO);
        URI location  = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(fornecedorCriado.id())
                .toUri();
        return ResponseEntity.created(location).body(fornecedorCriado);
    }
    @GetMapping
    public ResponseEntity<List<FornecedorRespostaDTO>>  obterFornecedores () {
        var fornecedores = serviceFornecedor.obter();
        return ResponseEntity.ok().body(fornecedores);
    }
    @DeleteMapping("{id}")
    public ResponseEntity<Void> removerFornecedor (@PathVariable("id")UUID fornecedor_id) {
         serviceFornecedor.remover(fornecedor_id);
         return  ResponseEntity.noContent().build();

    }
    @PutMapping("{id}")
    public ResponseEntity<Void> atualizarFornecedor(@PathVariable("id") UUID fornecedor_id, @RequestBody @Valid FornecedorRequisicaoDTO fornecedorRequisicaoDTO) {
        serviceFornecedor.atualizar(fornecedor_id,fornecedorRequisicaoDTO);
        return ResponseEntity.noContent().build();
    };
}
