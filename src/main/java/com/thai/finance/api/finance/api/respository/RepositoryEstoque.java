package com.thai.finance.api.finance.api.respository;

import com.thai.finance.api.finance.api.domain.entities.Estoque;
import com.thai.finance.api.finance.api.domain.entities.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RepositoryEstoque extends JpaRepository<Estoque, UUID> {
    Optional<Estoque> findByProduto(Produto produto);
}
