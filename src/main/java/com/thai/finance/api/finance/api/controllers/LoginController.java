package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.dtos.loginDTO.CreateAccountRequestDTO;
import com.thai.finance.api.finance.api.dtos.loginDTO.LoginRequestDTO;
import com.thai.finance.api.finance.api.dtos.loginDTO.LoginResponseDTO;
import com.thai.finance.api.finance.api.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping
public class LoginController {
    private final JwtEncoder jwtEncoder;
    private final UserService userService;

    public LoginController(JwtEncoder jwtEncoder, UserService userService) {
        this.userService = userService;
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login (@RequestBody LoginRequestDTO loginRequestDTO) {
     return ResponseEntity.ok(userService.login(loginRequestDTO));
    }

    @PostMapping("/createaccount")
    public ResponseEntity<Void> createAccounnt (@RequestBody CreateAccountRequestDTO createAccountRequestDTO) {

        userService.createAccount(createAccountRequestDTO);
        return ResponseEntity.ok().build();
    }


}
