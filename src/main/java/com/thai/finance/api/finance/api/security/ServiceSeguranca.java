package com.thai.finance.api.finance.api.security;

import com.thai.finance.api.finance.api.domain.entities.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ServiceSeguranca {

    public Usuario getUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof  AutenticacaoCustomizada autenticacaoCustomizada) {
            return autenticacaoCustomizada.getUsuario();
        }
        return null;
    }

}
