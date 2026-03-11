package com.thai.finance.api.finance.api.exceptions;

public class TransacaoNaoAutorizada extends RuntimeException {
    public TransacaoNaoAutorizada(String product) {
        super("Not authorized Transaction" + product);
    }
}
