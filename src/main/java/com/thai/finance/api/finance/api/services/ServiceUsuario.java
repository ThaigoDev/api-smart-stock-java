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

    public LoginRespostaDTO autenticar(LoginRequisicaoDTO loginRequisicaoDTO) {
        var usuario = repositoryUsuario.findByEmail(loginRequisicaoDTO.email());
        if (usuario.isEmpty() || !usuario.get().isLoginCorrect(loginRequisicaoDTO, passwordEncoder)) {
            throw new BadCredentialsException("Usuário ou senha inválidos");
        }
        ;
        var expiraEm = 1000L;
        Instant horarioAtual = Instant.now();
        var scopes = usuario.get().getFuncoes().stream().map(Funcao::getNome).collect(Collectors.joining(" "));
        var claims = JwtClaimsSet.builder().issuer("mybackend")
                .subject(
                        usuario.get().getId().toString()
                )
                .expiresAt(horarioAtual.plusSeconds(expiraEm))
                .issuedAt(horarioAtual)
                .claim("scope", scopes)
                .build();

        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new LoginRespostaDTO(jwtValue, expiraEm);

    }

    public void salvar(CriarContaRequisicaoDTO criarContaRequisicaoDTO) {
        var funcaoBasica = repositoryFuncao.findByName(Funcao.Values.BASIC.name());
        var usuarioDoBanco = repositoryUsuario.findByEmail(criarContaRequisicaoDTO.nome());
        if (usuarioDoBanco.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT);

        }
        Usuario usuario = new Usuario();
        usuario.setNome(criarContaRequisicaoDTO.nome());
        usuario.setEmail(criarContaRequisicaoDTO.email());
        usuario.setSenha(passwordEncoder.encode(criarContaRequisicaoDTO.senha()));
        usuario.setFuncoes(Set.of(funcaoBasica));
        repositoryUsuario.save(usuario);
    }
}
