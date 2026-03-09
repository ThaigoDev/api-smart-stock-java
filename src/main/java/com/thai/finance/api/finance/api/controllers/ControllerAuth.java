package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.domain.dtos.AuthDTO.CriarContaRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.AuthDTO.LoginRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.AuthDTO.LoginRespostaDTO;
import com.thai.finance.api.finance.api.services.ServiceUsuario;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/auth")
public class ControllerAuth {
    private final JwtEncoder jwtEncoder;
    private final ServiceUsuario serviceUsuario;

    public ControllerAuth(JwtEncoder jwtEncoder, ServiceUsuario serviceUsuario) {
        this.serviceUsuario = serviceUsuario;
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping
    public ResponseEntity<LoginRespostaDTO> login (@RequestBody @Valid LoginRequisicaoDTO loginRequisicaoDTO) {
     return ResponseEntity.ok(serviceUsuario.login(loginRequisicaoDTO));
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> criarConta (@RequestBody @Valid CriarContaRequisicaoDTO criarContaRequisicaoDTO) {

        serviceUsuario.createAccount(criarContaRequisicaoDTO);
        return ResponseEntity.ok().build();
    }


}
