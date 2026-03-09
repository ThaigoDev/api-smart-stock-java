package com.thai.finance.api.finance.api.respository;

import com.thai.finance.api.finance.api.domain.entities.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RepositoryFornecedor extends JpaRepository<Fornecedor, UUID> {
}
