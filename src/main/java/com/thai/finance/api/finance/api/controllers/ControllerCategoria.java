package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.domain.dtos.CategoriaDTO.CategoriaRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.CategoriaDTO.CategoriaRespostaDTO;
import com.thai.finance.api.finance.api.services.ServiceCategoria;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("categorias")
@RequiredArgsConstructor
public class ControllerCategoria {
     private final  ServiceCategoria serviceCategoria;


    @PostMapping
    public ResponseEntity<CategoriaRespostaDTO> criarCategoria(@RequestBody @Valid CategoriaRequisicaoDTO categoriaRequisicaoDTO) {
        var categoriaCriada =  serviceCategoria.salvar(categoriaRequisicaoDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(categoriaCriada.id())
                .toUri();
        return ResponseEntity.created(location).body(categoriaCriada);
    };
     @GetMapping
    public ResponseEntity<List<CategoriaRespostaDTO>> obterCategorias (){
        var categorias = serviceCategoria.obter();
        return  ResponseEntity.ok(categorias);
     };
     @DeleteMapping("{id}")
    public ResponseEntity<Void> removerCategoria(@PathVariable("id") UUID categoria_id) {
          serviceCategoria.remover(categoria_id);
          return ResponseEntity.noContent().build();
     }
}
