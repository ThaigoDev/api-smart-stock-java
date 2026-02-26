package com.thai.finance.api.finance.api.services;

import com.thai.finance.api.finance.api.domain.dtos.loginDTO.CreateAccountRequestDTO;
import com.thai.finance.api.finance.api.domain.dtos.loginDTO.LoginRequestDTO;
import com.thai.finance.api.finance.api.domain.dtos.loginDTO.LoginResponseDTO;
import com.thai.finance.api.finance.api.domain.entities.Role;
import com.thai.finance.api.finance.api.domain.entities.User;
import com.thai.finance.api.finance.api.respository.RoleRepository;
import com.thai.finance.api.finance.api.respository.UserRepository;
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
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final RoleRepository roleRepository;
    public UserService (UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtEncoder jwtEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder =passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.roleRepository = roleRepository;
    }

    public LoginResponseDTO login (LoginRequestDTO loginRequestDTO) {
     var user = userRepository.findByEmail(loginRequestDTO.email());
     if(user.isEmpty() || !user.get().isLoginCorrect(loginRequestDTO, passwordEncoder)){
         throw  new BadCredentialsException("User or password is invalid");
     };
     var expiresIn = 1000L;
     Instant now =  Instant.now();
     var scopes  = user.get().getRoles().stream().map(Role::getName).collect(Collectors.joining(" "));
     var claims = JwtClaimsSet.builder().issuer("mybackend")
             .subject(
                     user.get().getUserId().toString()
             )
             .expiresAt(now.plusSeconds(expiresIn))
             .issuedAt(now)
             .claim("scope", scopes)
             .build();

     var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
     return new LoginResponseDTO(jwtValue, expiresIn);

    }

    public void createAccount(CreateAccountRequestDTO createAccountRequestDTO) {
        var basicRole = roleRepository.findByName(Role.Values.BASIC.name());
        var userFromDB = userRepository.findByEmail(createAccountRequestDTO.username());
        if(userFromDB.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT);

        }
        User user = new User();
        user.setUsername(createAccountRequestDTO.username());
        user.setEmail(createAccountRequestDTO.email());
        user.setPassword(passwordEncoder.encode(createAccountRequestDTO.password()));
        user.setRoles(Set.of(basicRole));
        userRepository.save(user);
    }
}
