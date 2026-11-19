package com.thai.finance.api.finance.api.controllers;

import com.thai.finance.api.finance.api.domain.dtos.PedidoCompraDTO.PedidoCompraRequisicaoDTO;
import com.thai.finance.api.finance.api.domain.dtos.PedidoCompraDTO.PedidoCompraRespostaDTO;
import com.thai.finance.api.finance.api.domain.enums.StatusPedidoCompra;
import com.thai.finance.api.finance.api.services.ServicePedidoCompra;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Pedidos de compra", description = "Sugestão automática a partir dos alertas, recebimento e cancelamento")
@RestController
@RequestMapping("pedidos-compra")
@RequiredArgsConstructor
public class ControllerPedidoCompra {
    private final ServicePedidoCompra servicePedidoCompra;

    @GetMapping
    public ResponseEntity<List<PedidoCompraRespostaDTO>> listarPedidos(
            @RequestParam(value = "status", required = false) StatusPedidoCompra status) {
        return ResponseEntity.ok(servicePedidoCompra.listar(status));
    }

    @GetMapping("{id}")
    public ResponseEntity<PedidoCompraRespostaDTO> obterPedido(@PathVariable("id") UUID pedidoId) {
        return ResponseEntity.ok(servicePedidoCompra.obter(pedidoId));
    }

    @PostMapping
    public ResponseEntity<PedidoCompraRespostaDTO> criarPedido(@RequestBody @Valid PedidoCompraRequisicaoDTO pedidoCompraRequisicaoDTO) {
        var pedido = servicePedidoCompra.criar(pedidoCompraRequisicaoDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(pedido.id()).toUri();
        return ResponseEntity.created(location).body(pedido);
    }

    // cria um pedido por fornecedor com os produtos que atingiram o ponto de pedido
    @PostMapping("sugestoes")
    public ResponseEntity<List<PedidoCompraRespostaDTO>> gerarSugestoes(
            @RequestParam(value = "data_referencia", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia) {
        return ResponseEntity.ok(servicePedidoCompra.gerarSugestoes(dataReferencia));
    }

    @PostMapping("{id}/recebimento")
    public ResponseEntity<PedidoCompraRespostaDTO> receberPedido(@PathVariable("id") UUID pedidoId) {
        return ResponseEntity.ok(servicePedidoCompra.receber(pedidoId));
    }

    @PostMapping("{id}/cancelamento")
    public ResponseEntity<PedidoCompraRespostaDTO> cancelarPedido(@PathVariable("id") UUID pedidoId) {
        return ResponseEntity.ok(servicePedidoCompra.cancelar(pedidoId));
    }
}
