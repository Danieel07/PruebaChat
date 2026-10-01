package com.example.chat.impl;

import com.example.chat.ModelService;
import com.example.chat.dto.ChatRequest;
import com.example.chat.dto.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SpringAiModelService implements ModelService {

    private final ChatClient chatClient;

    public SpringAiModelService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("Eres un asistente útil y conciso.")
                .build();
    }

    public ChatResponse processChat(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        String requestId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();

        try {
            // Usamos OllamaOptions.builder() 
        var builder = OllamaChatOptions.builder();

        if (request.getTemperature() != null) { builder.temperature(request.getTemperature()); }
        if (request.getTopP() != null) { builder.topP(request.getTopP()); }
        if (request.getTopK() != null) { builder.topK(request.getTopK()); }
        if (request.getNumPredict() != null) { builder.numPredict(request.getNumPredict()); }

        org.springframework.ai.chat.model.ChatResponse aiResponse = chatClient.prompt()
        .user(request.getQuestion())
        .options(builder)   // el builder, sin .build()
        .call()
        .chatResponse();

            long elapsedMs = System.currentTimeMillis() - startTime;
            String answer = aiResponse.getResult().getOutput().getText();

            Usage usage = aiResponse.getMetadata().getUsage();
            Integer promptTokens = usage != null ? usage.getPromptTokens() : 0;
            Integer completionTokens = usage != null ? usage.getCompletionTokens() : 0;
            Integer totalTokens = usage != null ? usage.getTotalTokens() : 0;
            
            String model = aiResponse.getMetadata() != null ? aiResponse.getMetadata().getModel() : "unknown";
            String finishReason = aiResponse.getResult().getMetadata() != null ? aiResponse.getResult().getMetadata().getFinishReason() : "unknown";

            Double tokensPerSecond = 0.0;
            if (elapsedMs > 0 && completionTokens != null && completionTokens > 0) {
                tokensPerSecond = completionTokens / (elapsedMs / 1000.0);
            }

            return new ChatResponse(
                    requestId,
                    answer,
                    elapsedMs,
                    "ok",
                    promptTokens,
                    completionTokens,
                    totalTokens,
                    model,
                    finishReason,
                    tokensPerSecond
            );

        } catch (Exception e) {
            long elapsedMs = System.currentTimeMillis() - startTime;
            return new ChatResponse(
                    requestId,
                    "Error: " + e.getMessage(),
                    elapsedMs,
                    "error",
                    0, 0, 0, "error", "error", 0.0
            );
        }
    }

    @Override
    public ChatResponse generateResponse(String sessionId, String question) {
        ChatRequest req = new ChatRequest();
        req.setSessionId(sessionId);
        req.setQuestion(question);
        return processChat(req);
    }
}