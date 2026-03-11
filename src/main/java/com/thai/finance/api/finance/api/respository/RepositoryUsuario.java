package com.thai.finance.api.finance.api.respository;

import com.thai.finance.api.finance.api.domain.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RepositoryUsuario extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByEmail(String email);

    Usuario findByNome(String nome);
}
