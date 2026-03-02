package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.domain.dtos.AuthDTO.CreateAccountRequestDTO;
import com.thai.finance.api.finance.api.domain.dtos.AuthDTO.LoginRequestDTO;
import com.thai.finance.api.finance.api.domain.dtos.AuthDTO.LoginResponseDTO;
import com.thai.finance.api.finance.api.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JwtEncoder jwtEncoder;
    private final UserService userService;

    public AuthController(JwtEncoder jwtEncoder, UserService userService) {
        this.userService = userService;
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping
    public ResponseEntity<LoginResponseDTO> login (@RequestBody @Valid LoginRequestDTO loginRequestDTO) {
     return ResponseEntity.ok(userService.login(loginRequestDTO));
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> createAccounnt (@RequestBody @Valid CreateAccountRequestDTO createAccountRequestDTO) {

        userService.createAccount(createAccountRequestDTO);
        return ResponseEntity.ok().build();
    }


}
