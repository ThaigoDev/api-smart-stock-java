package com.thai.finance.api.finance.api.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Fornecedor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String cnpj;

    @Column
    private String razaoSocial;

    @Column
    private String email;

    @Column
    private String telefone;

    @OneToMany(mappedBy = "fornecedor")
    @JoinColumn(name = "produtosFornecidos")
    private List<ProdutoFornecedor> produtosFornecidos;

    @CreatedDate
    private LocalDateTime criado_em;

    @LastModifiedDate
    private LocalDateTime ultima_atualizacao;
    public Fornecedor(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }
}

