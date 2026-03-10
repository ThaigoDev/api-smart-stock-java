package com.thai.finance.api.finance.api.mapper;

import com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO.MovimentacaoEstoqueRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO.MovimentacaoEstoqueRespostaDTO;
import com.thai.finance.api.finance.api.domain.entities.MovimentacaoEstoque;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MapperMovimentacaoEstoque {
    @Mapping(target ="produto", ignore = true)
    MovimentacaoEstoque paraEntidade(MovimentacaoEstoqueRequisicaoDTO movimentacaoEstoqueRequisicaoDTO);
    @Mapping(target = "produto_id", source = "produto.id")
    MovimentacaoEstoqueRespostaDTO paraDTO(MovimentacaoEstoque movimentacaoEstoque);

}
