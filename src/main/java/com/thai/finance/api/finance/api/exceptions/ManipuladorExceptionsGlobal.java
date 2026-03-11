package com.thai.finance.api.finance.api.exceptions;

import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ManipuladorExceptionsGlobal {
    @ExceptionHandler(TransacaoNaoAutorizada.class)
    public ResponseEntity<ErrorMensage> handleNotAuthorizedTransaction(TransacaoNaoAutorizada ex) {
        ErrorMensage error = new ErrorMensage(
                400,
                "Bad request",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public  ResponseEntity<List<ErrorMensage>> handleMethodArgumentNotValidException (MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getFieldErrors();
        List<ErrorMensage> listOfErros = fieldErrors.stream().map(err-> new ErrorMensage(e.getStatusCode().value(), e.getFieldError().getField(),e.getMessage())).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(listOfErros);
    }

    @ExceptionHandler(UsuarioNaoEncontrado.class)
    public ResponseEntity<ErrorMensage> handleUsuarioNaoEncontradoException( UsuarioNaoEncontrado e) {
        ErrorMensage error = new ErrorMensage(404, "Not found", e.getMessage());
        return ResponseEntity.status(404).body(error);
    }
}
