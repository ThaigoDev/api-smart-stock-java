package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.domain.dtos.ProdutoDTO.ProdutoRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.ProdutoDTO.ProdutoRespostaDTO;
import com.thai.finance.api.finance.api.services.ServiceProduto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("produtos")
@RequiredArgsConstructor
public class ControllerProduto {
    private ServiceProduto serviceProduto;



    @PostMapping
    public ResponseEntity<ProdutoRespostaDTO> salvarProduto(@RequestBody  @Valid ProdutoRequisicaoDTO produtoRequisicaoDTO) {
        ProdutoRespostaDTO produtoCriado = serviceProduto.salvar(produtoRequisicaoDTO);

        URI location  = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(produtoCriado.id())
                .toUri();

        return ResponseEntity.created(location).body(produtoCriado);
    }

    ;

    @GetMapping
    public ResponseEntity<List<ProdutoRespostaDTO>> obterProdutos() {
        var produtos = serviceProduto.obter();
        return ResponseEntity.ok().body(produtos);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> removerProduto(@PathVariable("id") UUID produto_id) {
        serviceProduto.remover(produto_id);
        return ResponseEntity.noContent().build();

    }

    @PutMapping("{id}")
    public ResponseEntity<Void> atualizarProduto(@PathVariable("id") UUID produto_id, @RequestBody @Valid ProdutoRequisicaoDTO produtoRequisicaoDTO) {
        serviceProduto.atualizar(produto_id, produtoRequisicaoDTO);
        return ResponseEntity.noContent().build();
    }

    ;

    @GetMapping("pesquisar")
    public ResponseEntity<List<ProdutoRespostaDTO>> pesquisarProdutos(
            @RequestParam( value = "nome", required = false) String nome,
            @RequestParam(value = "sku", required = false) String sku,
            @RequestParam(value ="categoria", required = false) String categoria
    ) {
        return ResponseEntity.ok().body(serviceProduto.buscar(nome,sku,categoria));

    }

}
