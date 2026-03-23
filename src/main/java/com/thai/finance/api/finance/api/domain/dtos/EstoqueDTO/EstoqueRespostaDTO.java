package com.thai.finance.api.finance.api.domain.dtos.EstoqueDTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record EstoqueRespostaDTO(UUID id , UUID produto_id, Integer quantidade, LocalDateTime ultima_atualizacao ) {
}
