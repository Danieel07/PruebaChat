package com.example.chat;

import com.example.chat.dto.ChatRequest;
import com.example.chat.dto.ChatResponse;
import com.example.chat.dto.ClassifyRequest;
import com.example.chat.dto.ClassifyResponse;
import jakarta.validation.Valid;
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
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        try {
            return modelService.generateResponse(request);
        } catch (Exception e) {
            return new ChatResponse(
                    request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString(),
                    "Error interno: " + e.getMessage(),
                    0,
                    "error",
                    0, 0, 0, "error", "error", 0.0
            );
        }
    }

    @PostMapping("/classify")
    public ClassifyResponse classify(@Valid @RequestBody ClassifyRequest request) {
        return modelService.classify(request);
    }
}