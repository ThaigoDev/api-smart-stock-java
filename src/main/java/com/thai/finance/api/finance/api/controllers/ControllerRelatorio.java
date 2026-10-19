package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.domain.dtos.RelatorioDTO.*;
import com.thai.finance.api.finance.api.services.ServiceRelatorio;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("relatorios")
@RequiredArgsConstructor
public class ControllerRelatorio {
    private final ServiceRelatorio serviceRelatorio;

    @GetMapping("painel")
    public ResponseEntity<PainelDTO> painel(
            @RequestParam(value = "data_referencia", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia) {
        return ResponseEntity.ok(serviceRelatorio.painel(dataReferencia));
    }

    @GetMapping("alertas")
    public ResponseEntity<List<AlertaEstoqueDTO>> alertas(
            @RequestParam(value = "data_referencia", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia,
            @RequestParam(value = "todos", defaultValue = "false") boolean todos) {
        return ResponseEntity.ok(serviceRelatorio.alertas(dataReferencia, !todos));
    }

    @GetMapping("curva-abc")
    public ResponseEntity<CurvaABCDTO> curvaABC(
            @RequestParam(value = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(value = "fim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(serviceRelatorio.curvaABC(inicio, fim));
    }

    @GetMapping("giro")
    public ResponseEntity<GiroDTO> giro(
            @RequestParam(value = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(value = "fim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(serviceRelatorio.giro(inicio, fim));
    }

    @GetMapping("movimentacoes-mensais")
    public ResponseEntity<List<ResumoMensalDTO>> movimentacoesMensais(
            @RequestParam(value = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(value = "fim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(serviceRelatorio.resumoMensal(inicio, fim));
    }

    @GetMapping("conciliacao")
    public ResponseEntity<ConciliacaoDTO> conciliacao() {
        return ResponseEntity.ok(serviceRelatorio.conciliacao());
    }
}
