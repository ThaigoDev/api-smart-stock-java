package com.thai.finance.api.finance.api.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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
        List<ErrorMensage> listOfErros = e.getBindingResult().getAllErrors().stream()
                .map(err -> new ErrorMensage(400,
                        err instanceof org.springframework.validation.FieldError fe ? fe.getField() : err.getObjectName(),
                        err.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(listOfErros);
    }

    // JSON malformado ou valor fora do domínio (ex.: "tipo": "VENDA")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMensage> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(new ErrorMensage(400, "Bad request", "Corpo da requisição inválido ou com valor não reconhecido"));
    }

    // parâmetro de URL com tipo inválido (ex.: UUID malformado)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorMensage> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(new ErrorMensage(400, "Bad request", "Valor inválido para o parâmetro '" + e.getName() + "'"));
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErrorMensage> handleRegraNegocio(RegraNegocioException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(new ErrorMensage(422, "Unprocessable content", e.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorMensage> handleResponseStatus(ResponseStatusException e) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        return ResponseEntity.status(status).body(new ErrorMensage(status.value(), status.getReasonPhrase(), e.getReason()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorMensage> handleDataIntegrity(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorMensage(409, "Conflict",
                "Operação viola a integridade dos dados: o registro possui vínculos (ex.: estoque ou movimentações)"));
    }

    @ExceptionHandler(UsuarioNaoEncontrado.class)
    public ResponseEntity<ErrorMensage> handleUsuarioNaoEncontradoException( UsuarioNaoEncontrado e) {
        ErrorMensage error = new ErrorMensage(404, "Not found", e.getMessage());
        return ResponseEntity.status(404).body(error);
    }
}
