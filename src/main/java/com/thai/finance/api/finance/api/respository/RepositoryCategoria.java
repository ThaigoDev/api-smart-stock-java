package com.thai.finance.api.finance.api.respository;

import com.thai.finance.api.finance.api.domain.entities.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RepositoryCategoria extends JpaRepository<Categoria, UUID> {
}
