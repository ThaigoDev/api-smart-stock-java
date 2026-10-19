package com.thai.finance.api.finance.api.domain.dtos;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Envelope das listagens paginadas: evita devolver o histórico inteiro em uma única resposta. */
public record PaginaDTO<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long total_elementos,
        int total_paginas
) {
    public static <E, T> PaginaDTO<T> de(Page<E> pagina, Function<E, T> conversor) {
        return new PaginaDTO<>(pagina.getContent().stream().map(conversor).toList(), pagina.getNumber(),
                pagina.getSize(), pagina.getTotalElements(), pagina.getTotalPages());
    }
}
