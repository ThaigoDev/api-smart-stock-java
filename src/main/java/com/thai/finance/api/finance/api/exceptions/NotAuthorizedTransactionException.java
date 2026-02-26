package com.thai.finance.api.finance.api.exceptions;

public class NotAuthorizedTransactionException extends RuntimeException {
    public NotAuthorizedTransactionException(String product) {
        super("Not authorized Transaction" + product);
    }
}
