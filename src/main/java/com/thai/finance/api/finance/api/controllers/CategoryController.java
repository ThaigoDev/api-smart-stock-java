package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.domain.dtos.categoryDTO.CreateCategoryDTO;
import com.thai.finance.api.finance.api.domain.dtos.categoryDTO.ResponseCategoryDTO;
import com.thai.finance.api.finance.api.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/categories")
public class CategoryController {
     private CategoryService categoryService;
     public CategoryController(CategoryService categoryService) {
         this.categoryService = categoryService;
     }

    @PostMapping
    public ResponseEntity<ResponseCategoryDTO> createCategory(@RequestBody @Valid CreateCategoryDTO createCategoryDTO) {
        var createdCategory =  categoryService.createCategory(createCategoryDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(createdCategory.id())
                .toUri();
        return ResponseEntity.created(location).body(createdCategory);
    };
     @GetMapping
    public ResponseEntity<List<ResponseCategoryDTO>> getAllCategories (){
        var allCategories = categoryService.getAllCategories();
        return  ResponseEntity.ok(allCategories);
     };
     @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") UUID categoryId) {
          categoryService.deleteCategoryById(categoryId);
          return ResponseEntity.noContent().build();
     }
}
