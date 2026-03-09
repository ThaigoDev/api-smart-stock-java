package com.thai.finance.api.finance.api.respository;

import com.thai.finance.api.finance.api.domain.entities.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RepositoryProduto extends JpaRepository<Produto, UUID> {
    List<Produto> findByNameProduct(String name);
}