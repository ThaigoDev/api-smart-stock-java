package com.thai.finance.api.finance.api.respository;

import com.thai.finance.api.finance.api.domain.entities.MovimentacaoEstoque;
import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositoryMovimentacaoEstoque extends JpaRepository<MovimentacaoEstoque, UUID>, JpaSpecificationExecutor<MovimentacaoEstoque> {

    @Query("""
            select coalesce(sum(m.quantidade), 0) from MovimentacaoEstoque m
            where m.produto.id = :produtoId and m.tipo = :tipo and m.estornada = false
              and m.data_movimentacao >= :desde and m.data_movimentacao < :ate
            """)
    long somarQuantidade(@Param("produtoId") UUID produtoId, @Param("tipo") TipoMovimentacaoEstoque tipo,
                         @Param("desde") LocalDateTime desde, @Param("ate") LocalDateTime ate);

    @Query("""
            select m.produto.id, coalesce(sum(m.quantidade), 0) from MovimentacaoEstoque m
            where m.tipo = :tipo and m.estornada = false
              and m.data_movimentacao >= :desde and m.data_movimentacao < :ate
            group by m.produto.id
            """)
    List<Object[]> somarQuantidadePorProduto(@Param("tipo") TipoMovimentacaoEstoque tipo,
                                             @Param("desde") LocalDateTime desde, @Param("ate") LocalDateTime ate);

    @Query("""
            select m from MovimentacaoEstoque m join fetch m.produto
            where m.data_movimentacao < :ate
            order by m.data_movimentacao, m.sequencia
            """)
    List<MovimentacaoEstoque> buscarAte(@Param("ate") LocalDateTime ate);

    @Query("""
            select m from MovimentacaoEstoque m
            where m.produto.id = :produtoId
            order by m.sequencia
            """)
    List<MovimentacaoEstoque> buscarExtrato(@Param("produtoId") UUID produtoId);

    @Query("select m from MovimentacaoEstoque m join fetch m.produto order by m.produto.id, m.sequencia")
    List<MovimentacaoEstoque> buscarTodasOrdenadas();

    // trava a movimentação original para que dois estornos simultâneos não sejam ambos aceitos
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MovimentacaoEstoque m where m.id = :id")
    Optional<MovimentacaoEstoque> buscarParaEstorno(@Param("id") UUID id);
}
