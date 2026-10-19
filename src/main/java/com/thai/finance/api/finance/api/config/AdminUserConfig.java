package com.thai.finance.api.finance.api.config;

import com.thai.finance.api.finance.api.domain.entities.Funcao;
import com.thai.finance.api.finance.api.domain.entities.Usuario;
import com.thai.finance.api.finance.api.respository.RepositoryFuncao;
import com.thai.finance.api.finance.api.respository.RepositoryUsuario;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Order(1)
public class AdminUserConfig  implements CommandLineRunner {
    private final RepositoryFuncao repositoryFuncao;
    private final RepositoryUsuario repositoryUsuario;
    private  final BCryptPasswordEncoder passwordEncoder;

    // credenciais do administrador inicial vêm da configuração (variáveis ADMIN_EMAIL e ADMIN_SENHA)
    @Value("${smartstock.admin.email}")
    private String emailAdmin;
    @Value("${smartstock.admin.senha}")
    private String senhaAdmin;

    @Override
    @Transactional
    public void run(String ...args) throws  Exception {
        // em um banco recém-criado as funções ainda não existem: cria em vez de abortar a inicialização
        var FuncaoAdmin = obterOuCriarFuncao(Funcao.Values.ADMIN.name().toLowerCase());
        obterOuCriarFuncao(Funcao.Values.USUARIO.name().toLowerCase());

        var usuarioAdmin  = repositoryUsuario.findByEmail(emailAdmin);
      usuarioAdmin.ifPresentOrElse((usuario)-> System.out.println("Usuário admin já existe"), ()-> {
          var usuario = new Usuario();
          usuario.setNome("admin");
          usuario.setEmail(emailAdmin);
          usuario.setSenha( passwordEncoder.encode(senhaAdmin));
          usuario.setFuncoes(Set.of(FuncaoAdmin));

          repositoryUsuario.save(usuario);

      });
    }

    private Funcao obterOuCriarFuncao(String nome) {
        var funcao = repositoryFuncao.findByNome(nome);
        if (funcao != null) {
            return funcao;
        }
        var nova = new Funcao();
        nova.setNome(nome);
        return repositoryFuncao.save(nova);
    }
}
