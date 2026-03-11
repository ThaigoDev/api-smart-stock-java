package com.thai.finance.api.finance.api.security;

import com.thai.finance.api.finance.api.domain.entities.Usuario;
import com.thai.finance.api.finance.api.services.ServiceUsuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ManipuladorLoginSocial extends SavedRequestAwareAuthenticationSuccessHandler {
    private final ServiceUsuario serviceUsuario;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {

        OAuth2AuthenticationToken oAuth2AuthenticationToken =  (OAuth2AuthenticationToken) authentication;

        OAuth2User oAuth2Usuario = oAuth2AuthenticationToken.getPrincipal();
        String email = oAuth2Usuario.getAttribute("email");

        Optional<Usuario>  usuario = serviceUsuario.obterPorEmail(email);
        if(usuario.isPresent()) {
            AutenticacaoCustomizada autenticacaoCustomizada = new AutenticacaoCustomizada(usuario.get());
            SecurityContextHolder.getContext().setAuthentication(autenticacaoCustomizada);
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
