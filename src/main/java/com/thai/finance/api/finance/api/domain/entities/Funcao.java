package com.thai.finance.api.finance.api.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Funcao {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long funcaoId;
    private String nome;

    public enum Values {
        BASIC(1L),
        ADMIN (2L);
        long funcaoId;
        Values(Long funcaoId) {
            this.funcaoId = funcaoId;
        }
        public long getRoleId() {
            return funcaoId;
        }
    }

    @CreatedDate
    private LocalDateTime createAt;

    @LastModifiedDate
    private LocalDateTime updateAt;

    public Funcao(Long funcaoId, String nome) {
        this.funcaoId = funcaoId;
        this.nome = nome;
    }
}
