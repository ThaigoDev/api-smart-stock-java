package com.thai.finance.api.finance.api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Identifica quem está executando a operação, para a trilha de auditoria.
 * Operações sem usuário autenticado (ex.: geração da massa de dados na inicialização) são atribuídas ao "sistema".
 */
@Component
public class UsuarioAtual {

    public static final String SISTEMA = "sistema";

    public String identificador() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !autenticacao.isAuthenticated()) {
            return SISTEMA;
        }
        if (autenticacao instanceof JwtAuthenticationToken jwt) {
            String email = jwt.getToken().getClaimAsString("email");
            if (email != null && !email.isBlank()) {
                return email;
            }
        }
        String nome = autenticacao.getName();
        return nome == null || nome.isBlank() || "anonymousUser".equals(nome) ? SISTEMA : nome;
    }
}
