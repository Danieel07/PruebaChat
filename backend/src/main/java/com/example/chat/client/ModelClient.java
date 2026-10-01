package com.example.chat.client;

public interface ModelClient {
    /**
     * Send question to model and return textual answer. Implementations integrate
     * with specific runtimes (e.g., local NVIDIA model, remote API).
     */
    String ask(String prompt);
}
