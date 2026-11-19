package com.thai.finance.api.finance.api.respository;

import com.thai.finance.api.finance.api.domain.entities.PedidoCompra;
import com.thai.finance.api.finance.api.domain.enums.StatusPedidoCompra;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RepositoryPedidoCompra extends JpaRepository<PedidoCompra, UUID> {

    @Query("select distinct p from PedidoCompra p join fetch p.fornecedor left join fetch p.itens i left join fetch i.produto order by p.criado_em desc")
    List<PedidoCompra> buscarTodosComItens();

    @Query("select distinct p from PedidoCompra p join fetch p.fornecedor left join fetch p.itens i left join fetch i.produto where p.status = :status order by p.criado_em desc")
    List<PedidoCompra> buscarPorStatus(@Param("status") StatusPedidoCompra status);

    // trava o pedido: dois recebimentos simultâneos do mesmo pedido não podem ambos lançar as entradas
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PedidoCompra p where p.id = :id")
    Optional<PedidoCompra> buscarParaAtualizacao(@Param("id") UUID id);

    @Query("select i.produto.id from ItemPedidoCompra i where i.pedido.status = com.thai.finance.api.finance.api.domain.enums.StatusPedidoCompra.ABERTO")
    Set<UUID> produtosEmPedidosAbertos();
}
