package com.thai.finance.api.finance.api.respository;

import com.thai.finance.api.finance.api.domain.entities.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RepositoryProduto extends JpaRepository<Produto, UUID> {
    List<Produto> findByNome(String nome);

    @Query("select p from Produto p join fetch p.categoria join fetch p.fornecedor")
    List<Produto> buscarTodosComCadastros();
}