package com.example.chat;

import com.example.chat.dto.ChatRequest;
import com.example.chat.dto.ChatResponse;
import com.example.chat.dto.ClassifyRequest;
import com.example.chat.dto.ClassifyResponse;

public interface ModelService {

    ChatResponse generateResponse(ChatRequest request);

    default ChatResponse generateResponse(String sessionId, String question) {
        return generateResponse(new ChatRequest(sessionId, question));
    }

    ClassifyResponse classify(ClassifyRequest request);
}