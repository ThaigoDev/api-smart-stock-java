package com.thai.finance.api.finance.api.config;

import com.thai.finance.api.finance.api.domain.entities.Funcao;
import com.thai.finance.api.finance.api.domain.entities.Usuario;
import com.thai.finance.api.finance.api.respository.RepositoryFuncao;
import com.thai.finance.api.finance.api.respository.RepositoryUsuario;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class AdminUserConfig  implements CommandLineRunner {
    private final RepositoryFuncao repositoryFuncao;
    private final RepositoryUsuario repositoryUsuario;
    private  final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String ...args) throws  Exception {
        var FuncaoAdmin = repositoryFuncao.findByNome(Funcao.Values.ADMIN.name());
        var usuarioAdmin  = repositoryUsuario.findByEmail("admin@admin.com");
      usuarioAdmin.ifPresentOrElse((usuario)-> System.out.println("Usuário admin já existe"), ()-> {
          var usuario = new Usuario();
          usuario.setNome("admin");
          usuario.setEmail("admin@admin.com");
          usuario.setSenha( passwordEncoder.encode("admin331400"));
          usuario.setFuncoes(Set.of(FuncaoAdmin));

          repositoryUsuario.save(usuario);

      });
    }
}
