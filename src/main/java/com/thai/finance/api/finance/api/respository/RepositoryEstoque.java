package com.thai.finance.api.finance.api.respository;

import com.thai.finance.api.finance.api.domain.entities.Estoque;
import com.thai.finance.api.finance.api.domain.entities.Produto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositoryEstoque extends JpaRepository<Estoque, UUID> {
    Optional<Estoque> findByProduto(Produto produto);

    // SELECT ... FOR UPDATE: movimentações simultâneas do mesmo produto são serializadas pelo banco,
    // impedindo que duas saídas leiam o mesmo saldo e ambas sejam aprovadas (lost update)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Estoque e where e.produto.id = :produtoId")
    Optional<Estoque> buscarParaAtualizacao(@Param("produtoId") UUID produtoId);

    @Query("select e from Estoque e join fetch e.produto p join fetch p.categoria")
    List<Estoque> buscarTodosComProduto();
}
