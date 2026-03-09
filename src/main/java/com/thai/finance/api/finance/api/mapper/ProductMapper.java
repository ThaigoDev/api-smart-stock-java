package com.thai.finance.api.finance.api.mapper;

import com.thai.finance.api.finance.api.domain.dtos.ProdutoDTO.ProdutoRespostaDTO;
import com.thai.finance.api.finance.api.domain.entities.Produto;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductMapper {
    public ProdutoRespostaDTO EntityResponseToDTO (Produto produto) {
         UUID stockId = null;
         Integer stockQuantity = null;

         if(produto.getStock()!= null) {
             stockId = produto.getStock().getId();
             stockQuantity= produto.getStock().getQuantityProduct();
         }

        ProdutoRespostaDTO entityConverted =  new ProdutoRespostaDTO(produto.getId(), produto.getNameProduct(), produto.getSkuProduct(), produto.getMinimum_stock(), produto.getCategoryId().getId(), produto.getSupplier().getId(),stockId,stockQuantity, produto.getInitialStock(), produto.isActive());
        return  entityConverted;

    };

}
