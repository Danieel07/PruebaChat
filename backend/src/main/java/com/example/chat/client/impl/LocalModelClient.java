package com.example.chat.client.impl;

import com.example.chat.client.ModelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * LocalModelClient: calls a local model runtime HTTP endpoint.
 */
@Component
public class LocalModelClient implements ModelClient {

    private final RestTemplate rest = new RestTemplate();
    private final String modelUrl;

    public LocalModelClient(@Value("${MODEL_HOST:model:5005}") String modelHost) {
        // modelHost is expected like host:port or host
        if (modelHost.contains(":")) {
            this.modelUrl = "http://" + modelHost + "/generate";
        } else {
            this.modelUrl = "http://" + modelHost + "/generate";
        }
    }

    @Override
    public String ask(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String,Object> body = Map.of("prompt", prompt);
            HttpEntity<Map<String,Object>> req = new HttpEntity<>(body, headers);
            Map resp = rest.postForObject(modelUrl, req, Map.class);
            if (resp == null) return "";
            Object out = resp.getOrDefault("answer", resp.get("text"));
            return out == null ? "" : out.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
