package com.thai.finance.api.finance.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExcpetionHandle {
    @ExceptionHandler(NotAuthorizedTransactionException.class)
    public ResponseEntity<ErrorMensage> handleNotAuthorizedTransaction(NotAuthorizedTransactionException ex) {
        ErrorMensage error = new ErrorMensage(
                400,
                "Bad request",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    }
}
