package com.thai.finance.api.finance.api.respository;

import com.thai.finance.api.finance.api.domain.entities.Funcao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositoryFuncao extends JpaRepository <Funcao, Long>{
    Funcao findByName(String name);
}
