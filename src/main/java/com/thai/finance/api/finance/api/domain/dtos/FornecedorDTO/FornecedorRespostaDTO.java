package com.thai.finance.api.finance.api.domain.dtos.FornecedorDTO;

import java.util.UUID;

public record FornecedorRespostaDTO(UUID id, String cnpj, String razaoSocial, String email, String telefone) {
}
