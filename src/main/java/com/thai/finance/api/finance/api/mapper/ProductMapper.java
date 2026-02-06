package com.thai.finance.api.finance.api.mapper;

import com.thai.finance.api.finance.api.dtos.productDTO.CreateProductDTO;
import com.thai.finance.api.finance.api.dtos.productDTO.ResponseProductDTO;
import com.thai.finance.api.finance.api.entities.Product;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ProductMapper {
    public  ResponseProductDTO EntityResponseToDTO (Product product) {

        ResponseProductDTO entityConverted =  new ResponseProductDTO(product.getNameProduct(), product.getSkuProduct(), product.getMinimum_stock(), product.getCategoryId().getId(), product.getSupplier().getId(),product.getStock().getId(), product.getInitialStock(), product.isActive());
        return  entityConverted;

    };

}
