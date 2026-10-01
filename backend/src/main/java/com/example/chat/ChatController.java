package com.example.chat;

import com.example.chat.dto.ChatRequest;
import com.example.chat.dto.ChatResponse;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ModelService modelService;

    public ChatController(ModelService modelService) {
        this.modelService = modelService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        try {
            // El servicio ahora nos devuelve el ChatResponse completo con la metadata
            return modelService.generateResponse(request.getSessionId(), request.getQuestion());
        } catch (Exception e) {
            // Si hay error crítico, armamos un ChatResponse de error con valores en 0
            return new ChatResponse(
                    request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString(),
                    "Error interno: " + e.getMessage(),
                    0,
                    "error",
                    0, 0, 0, "error", "error", 0.0
            );
        }
    }
}