package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.dtos.categoryDTO.CreateCategoryDTO;
import com.thai.finance.api.finance.api.dtos.categoryDTO.ResponseCategoryDTO;
import com.thai.finance.api.finance.api.entities.Category;
import com.thai.finance.api.finance.api.mapper.CategoryMapper;
import com.thai.finance.api.finance.api.respository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {
     private final CategoryRepository categoryRepository;
     private final CategoryMapper categoryMapper;
     public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
         this.categoryRepository = categoryRepository;
         this.categoryMapper = categoryMapper;
     }
    public ResponseCategoryDTO createCategory(CreateCategoryDTO createCategoryDTO) {
        Category categoryEntity = new Category(
                null,
                createCategoryDTO.name()
        );
       var createdCategoryConverted = categoryMapper.EntityResponseToDTO(categoryRepository.save(categoryEntity)) ;
         return  createdCategoryConverted;

    }

    public List<ResponseCategoryDTO> getAllCategories() {
         return  categoryRepository.findAll().stream().map(category -> categoryMapper.EntityResponseToDTO(category)).toList();
    };

    public void deleteCategoryById(UUID id) {
        var categoryExist = categoryRepository.findById(id).orElseThrow(()->  new ResponseStatusException (HttpStatus.NOT_FOUND, "Category not found") );
        categoryRepository.deleteById(categoryExist.getId());

    }
}
