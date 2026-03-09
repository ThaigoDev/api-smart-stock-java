package com.thai.finance.api.finance.api.respository;

import com.thai.finance.api.finance.api.domain.entities.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RepositoryMovimentacaoEstoque extends JpaRepository<MovimentacaoEstoque, UUID> {
}
