package com.example.chat;

import com.example.chat.dto.ChatResponse;

public interface ModelService {

    ChatResponse generateResponse(String sessionId, String question);
}