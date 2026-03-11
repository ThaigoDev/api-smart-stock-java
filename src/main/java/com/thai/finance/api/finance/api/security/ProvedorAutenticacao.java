package com.thai.finance.api.finance.api.security;

import com.thai.finance.api.finance.api.domain.entities.Usuario;
import com.thai.finance.api.finance.api.exceptions.UsuarioNaoEncontrado;
import com.thai.finance.api.finance.api.services.ServiceUsuario;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProvedorAutenticacao implements AuthenticationProvider {
    private final ServiceUsuario serviceUsuario;
    private final PasswordEncoder codificador;
    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var nome_usuario =  authentication.getName();
        String senhAutenticacao = authentication.getCredentials().toString();

         Usuario usuarioDoBanco = serviceUsuario.obterPorNome(nome_usuario);
         String senhaCriptografada = usuarioDoBanco.getSenha();

         if(usuarioDoBanco == null) {
             throw new UsuarioNaoEncontrado("Usuário não foi encontrado, senha ou/e usuário incorretos");
         }

         if(codificador.matches(senhAutenticacao, senhaCriptografada)) {
             new AutenticacaoCustomizada(usuarioDoBanco);
         }
        throw new UsuarioNaoEncontrado("Usuário não foi encontrado, senha ou/e usuário incorretos");

    }

    @Override
    public boolean supports(Class<?> authentication) {

        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }
}
