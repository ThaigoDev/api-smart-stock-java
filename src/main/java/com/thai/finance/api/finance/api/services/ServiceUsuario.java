package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.AuthDTO.CriarContaRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.AuthDTO.LoginRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.AuthDTO.LoginRespostaDTO;
import com.thai.finance.api.finance.api.domain.entities.Funcao;
import com.thai.finance.api.finance.api.domain.entities.Usuario;
import com.thai.finance.api.finance.api.respository.RepositoryFuncao;
import com.thai.finance.api.finance.api.respository.RepositoryUsuario;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ServiceUsuario {
    private final RepositoryUsuario repositoryUsuario;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final RepositoryFuncao repositoryFuncao;

    public ServiceUsuario(RepositoryUsuario repositoryUsuario, BCryptPasswordEncoder passwordEncoder, JwtEncoder jwtEncoder, RepositoryFuncao repositoryFuncao) {
        this.repositoryUsuario = repositoryUsuario;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.repositoryFuncao = repositoryFuncao;
    }

    public LoginRespostaDTO login(LoginRequisicaoDTO loginRequisicaoDTO) {
        var user = repositoryUsuario.findByEmail(loginRequisicaoDTO.email());
        if (user.isEmpty() || !user.get().isLoginCorrect(loginRequisicaoDTO, passwordEncoder)) {
            throw new BadCredentialsException("User or password is invalid");
        }
        ;
        var expiresIn = 1000L;
        Instant now = Instant.now();
        var scopes = user.get().getFuncaos().stream().map(Funcao::getName).collect(Collectors.joining(" "));
        var claims = JwtClaimsSet.builder().issuer("mybackend")
                .subject(
                        user.get().getUserId().toString()
                )
                .expiresAt(now.plusSeconds(expiresIn))
                .issuedAt(now)
                .claim("scope", scopes)
                .build();

        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new LoginRespostaDTO(jwtValue, expiresIn);

    }

    public void createAccount(CriarContaRequisicaoDTO criarContaRequisicaoDTO) {
        var basicRole = repositoryFuncao.findByName(Funcao.Values.BASIC.name());
        var userFromDB = repositoryUsuario.findByEmail(criarContaRequisicaoDTO.username());
        if (userFromDB.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT);

        }
        Usuario usuario = new Usuario();
        usuario.setUsername(criarContaRequisicaoDTO.username());
        usuario.setEmail(criarContaRequisicaoDTO.email());
        usuario.setPassword(passwordEncoder.encode(criarContaRequisicaoDTO.password()));
        usuario.setFuncaos(Set.of(basicRole));
        repositoryUsuario.save(usuario);
    }
}
