package com.example.chat.exception;

import com.example.chat.dto.ChatResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.UUID;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ChatResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ChatResponse errorResponse = new ChatResponse(
                UUID.randomUUID().toString(),
                "Error de validación: " + errors,
                0,
                "error",
                null, null, null, null, "validation_error", 0.0
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ChatResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ChatResponse errorResponse = new ChatResponse(
                UUID.randomUUID().toString(),
                "Argumento inválido: " + ex.getMessage(),
                0,
                "error",
                null, null, null, null, "invalid_argument", 0.0
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ChatResponse> handleRuntimeException(RuntimeException ex) {
        ChatResponse errorResponse = new ChatResponse(
                UUID.randomUUID().toString(),
                "Error interno del servidor: " + ex.getMessage(),
                0,
                "error",
                null,
                null,
                null,
                null,
                "error",
                0.0
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}