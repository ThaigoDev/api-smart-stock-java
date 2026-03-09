package com.thai.finance.api.finance.api.domain.entities;

import com.thai.finance.api.finance.api.domain.dtos.AuthDTO.LoginRequisicaoDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;

@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private UUID id;

    @Column
    private String nome;
    @Column
    private String email;
    @Column
    private String senha;

   @ManyToMany(cascade =  CascadeType.ALL, fetch = FetchType.EAGER)
   @JoinTable(
           name= "funcoes_usuario",
           joinColumns = @JoinColumn(name = "usuario_id"),
           inverseJoinColumns = @JoinColumn(name = "funcao_id")
   )

    private Set<Funcao> funcoes;

    public boolean isLoginCorrect(LoginRequisicaoDTO loginRequest, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(loginRequest.password(), this.senha);
    }


}
