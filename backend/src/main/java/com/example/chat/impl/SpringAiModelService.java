package com.example.chat.impl;

import com.example.chat.ModelService;
import com.example.chat.client.ModelClient;
import org.springframework.stereotype.Service;

@Service
public class SpringAiModelService implements ModelService {

    private final ModelClient client;

    public SpringAiModelService(ModelClient client) {
        this.client = client;
    }

    @Override
    public String generateResponse(String question, String sessionId) {
        // In a full implementation, integrate with Spring AI APIs or SDK.
        // For demo: delegate to ModelClient which talks to the local model runtime.
        return client.ask(question);
    }
}
