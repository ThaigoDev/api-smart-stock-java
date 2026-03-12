package com.thai.finance.api.finance.api.security;

import com.thai.finance.api.finance.api.services.ServiceClienteSeguranca;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RepositoryClienteSegurancaRegistradoCustomizado implements RegisteredClientRepository {
    private ServiceClienteSeguranca serviceClienteSeguranca;
    private TokenSettings tokenSettings;
    private ClientSettings clientSettings;

    @Bean
    @Order(1)
    public SecurityFilterChain oauthServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer oAuth2AuthorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        http.with( oAuth2AuthorizationServerConfigurer, server -> server.oidc(Customizer.withDefaults())
        );

        http.formLogin(Customizer.withDefaults());
        http.oauth2ResourceServer(oauthServer -> oauthServer.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Override
    public void save(RegisteredClient registeredClient) {

    }

    @Override
    public RegisteredClient findById(String id) {
        return null;
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
         var clienteEncontrado =  serviceClienteSeguranca.obterPorId(clientId);
        RegisteredClient
                .withId(clienteEncontrado.id().toString())
                .clientId(clienteEncontrado.clienteId())
                .clientSecret(clienteEncontrado.clienteSenha())
                .scope(clienteEncontrado.scopes())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_JWT)
                .tokenSettings(tokenSettings)
                .clientSettings(clientSettings)
                .redirectUri(clienteEncontrado.urlRedirecionamento())
                .build();
        return null;
    }
}
