package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO.EstornoRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO.MovimentacaoEstoqueRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.MovimentacaoEstoqueDTO.MovimentacaoEstoqueRespostaDTO;
import com.thai.finance.api.finance.api.domain.enums.TipoMovimentacaoEstoque;
import com.thai.finance.api.finance.api.services.ServiceMovimentacaoEstoque;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Movimentações formam um histórico imutável: não há PUT nem DELETE, correções são feitas por estorno
@RestController
@RequestMapping("movimentacoesestoque")
@RequiredArgsConstructor
public class ControllerMovimentacaoEstoque {
    private final ServiceMovimentacaoEstoque serviceMovimentacaoEstoque;


    @PostMapping
    public ResponseEntity<MovimentacaoEstoqueRespostaDTO> salvarMovimentacaoEstoque(@RequestBody @Valid MovimentacaoEstoqueRequisicaoDTO movimentacaoEstoqueRequisicaoDTO) {

        var movimentacaoEstoqueCriada = serviceMovimentacaoEstoque.salvar(movimentacaoEstoqueRequisicaoDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(movimentacaoEstoqueCriada.id())
                .toUri();

         return  ResponseEntity.created(location).body(movimentacaoEstoqueCriada);
    }

    @GetMapping
    public ResponseEntity<List<MovimentacaoEstoqueRespostaDTO>> obterMovimentacoesEstoques(
            @RequestParam(value = "produto_id", required = false) UUID produtoId,
            @RequestParam(value = "tipo", required = false) TipoMovimentacaoEstoque tipo,
            @RequestParam(value = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(value = "fim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return  ResponseEntity.ok().body(serviceMovimentacaoEstoque.obter(produtoId, tipo, inicio, fim));
    }

    @GetMapping("{id}")
    public ResponseEntity<MovimentacaoEstoqueRespostaDTO> obterMovimentacaoPorId(@PathVariable("id") UUID movimentacaoEstoque_id) {
        return ResponseEntity.ok().body(serviceMovimentacaoEstoque.obterPorId(movimentacaoEstoque_id));
    }

    @PostMapping("{id}/estorno")
    public ResponseEntity<MovimentacaoEstoqueRespostaDTO> estornarMovimentacao(@PathVariable("id") UUID movimentacaoEstoque_id, @RequestBody @Valid EstornoRequisicaoDTO estornoRequisicaoDTO) {
        var estorno = serviceMovimentacaoEstoque.estornar(movimentacaoEstoque_id, estornoRequisicaoDTO.motivo());
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/movimentacoesestoque/{id}")
                .buildAndExpand(estorno.id())
                .toUri();
        return ResponseEntity.created(location).body(estorno);
    }
 }
