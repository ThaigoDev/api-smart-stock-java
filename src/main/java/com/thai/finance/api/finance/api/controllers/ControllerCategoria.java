package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.domain.dtos.CategoriaDTO.CategoriaRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.CategoriaDTO.CategoriaRespostaDTO;
import com.thai.finance.api.finance.api.services.ServiceCategoria;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/categories")
public class ControllerCategoria {
     private ServiceCategoria serviceCategoria;
     public ControllerCategoria(ServiceCategoria serviceCategoria) {
         this.serviceCategoria = serviceCategoria;
     }

    @PostMapping
    public ResponseEntity<CategoriaRespostaDTO> createCategory(@RequestBody @Valid CategoriaRequisicaoDTO categoriaRequisicaoDTO) {
        var createdCategory =  serviceCategoria.createCategory(categoriaRequisicaoDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(createdCategory.id())
                .toUri();
        return ResponseEntity.created(location).body(createdCategory);
    };
     @GetMapping
    public ResponseEntity<List<CategoriaRespostaDTO>> getAllCategories (){
        var allCategories = serviceCategoria.getAllCategories();
        return  ResponseEntity.ok(allCategories);
     };
     @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") UUID categoryId) {
          serviceCategoria.deleteCategoryById(categoryId);
          return ResponseEntity.noContent().build();
     }
}
