package com.thai.finance.api.finance.api.config;

import com.thai.finance.api.finance.api.domain.entities.Funcao;
import com.thai.finance.api.finance.api.domain.entities.Usuario;
import com.thai.finance.api.finance.api.respository.RepositoryFuncao;
import com.thai.finance.api.finance.api.respository.RepositoryUsuario;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;

@Configuration
public class AdminUserConfig  implements CommandLineRunner {
    private RepositoryFuncao repositoryFuncao;
    private RepositoryUsuario repositoryUsuario;
    private BCryptPasswordEncoder passwordEncoder;
    public AdminUserConfig(RepositoryFuncao repositoryFuncao, RepositoryUsuario repositoryUsuario, BCryptPasswordEncoder passwordEncoder) {
        this.repositoryFuncao = repositoryFuncao;
        this.repositoryUsuario = repositoryUsuario;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String ...args) throws  Exception {
        var RoleAdmin = repositoryFuncao.findByName(Funcao.Values.ADMIN.name());
        var userAdmin  = repositoryUsuario.findByEmail("admin@admin.com");
      userAdmin.ifPresentOrElse((usuario)-> System.out.println("Admin exist."), ()-> {
          var user = new Usuario();
          user.setUsername("admin");
          user.setEmail("admin@admin.com");
          user.setPassword( passwordEncoder.encode("admin331400"));
          user.setFuncaos(Set.of(RoleAdmin));

          repositoryUsuario.save(user);

      });
    }
}
