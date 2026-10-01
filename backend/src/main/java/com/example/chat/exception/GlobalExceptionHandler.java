package com.example.chat.exception;

import com.example.chat.dto.ChatResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ChatResponse> handleRuntimeException(RuntimeException ex) {
        
        // Se actualiza la instanciación con los nuevos parámetros en null/0
        ChatResponse errorResponse = new ChatResponse(
                UUID.randomUUID().toString(), // Generamos un id temporal
                "Error interno del servidor: " + ex.getMessage(),
                0,      // elapsedMs
                "error", // status
                null,   // promptTokens
                null,   // completionTokens
                null,   // totalTokens
                null,   // model
                "error",// finishReason
                0.0     // tokensPerSecond
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}