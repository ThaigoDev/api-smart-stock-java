package com.thai.finance.api.finance.api.security;

import com.thai.finance.api.finance.api.domain.entities.Usuario;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;
@RequiredArgsConstructor
@Getter
public class AutenticacaoCustomizada implements Authentication {
    private final Usuario usuario;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return usuario.getFuncoes().stream().map(funcao -> new SimpleGrantedAuthority(funcao.getNome())).collect(Collectors.toList()) ;
    }

    @Override
    public @Nullable Object getCredentials() {
        return null;
    }

    @Override
    public @Nullable Object getDetails() {
        return usuario;
    }

    @Override
    public @Nullable Object getPrincipal() {
        return usuario;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

    @Override
    public String getName() {
        return usuario.getNome();
    }
}
