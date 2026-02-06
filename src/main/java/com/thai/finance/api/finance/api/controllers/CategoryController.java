package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.dtos.categoryDTO.CreateCategoryDTO;
import com.thai.finance.api.finance.api.dtos.categoryDTO.ResponseCategoryDTO;
import com.thai.finance.api.finance.api.services.CategoryService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ResponseCategoryDTO> createCategory(@RequestBody CreateCategoryDTO createCategoryDTO) {
        var createdCategory =  categoryService.createCategory(createCategoryDTO);

        return ResponseEntity.ok(createdCategory);
    };
     @GetMapping("/all")
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
