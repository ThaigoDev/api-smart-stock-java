package com.thai.finance.api.finance.api.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.thai.finance.api.finance.api.security.ManipuladorLoginSocial;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;
    @Value("${jwt.private.key}")
    private RSAPrivateKey privateKey;

    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http, ManipuladorLoginSocial manipuladorLoginSocial) {
       return http.authorizeHttpRequests( authorize -> authorize
                .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                .anyRequest().permitAll()
        ).csrf(csrf -> csrf.disable())
                .oauth2ResourceServer( auth -> auth.jwt(Customizer.withDefaults()))
                .sessionManagement( session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               .oauth2Login(oauth2 -> oauth2.successHandler(manipuladorLoginSocial))
               .httpBasic(Customizer.withDefaults())
               .formLogin(Customizer.withDefaults())
               .build();
    }
    @Bean
    public JwtEncoder jwtEncoder () {
        JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(this.privateKey).build();
         var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
         return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults () {
        return  new GrantedAuthorityDefaults(" ");
    }

   @Bean
    public JwtDecoder jwtDecoder () {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
   }

   @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }




}
