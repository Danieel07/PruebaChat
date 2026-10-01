package com.example.chat;

import com.example.chat.dto.ChatRequest;
import com.example.chat.dto.ChatResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ChatController {

    private final ModelService modelService;

    public ChatController(ModelService modelService) {
        this.modelService = modelService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest req) {
        long start = Instant.now().toEpochMilli();
        String answer = modelService.generateResponse(req.getQuestion(), req.getSessionId());
        long elapsed = Instant.now().toEpochMilli() - start;
        ChatResponse resp = new ChatResponse(UUID.randomUUID().toString(), answer, elapsed, "ok");
        return ResponseEntity.ok(resp);
    }
}
