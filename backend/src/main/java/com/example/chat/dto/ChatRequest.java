package com.example.chat.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class ChatRequest {
    private String sessionId;
    private String question;

    @DecimalMin("0.0") 
    @DecimalMax("2.0")
    private Double temperature;

    @DecimalMin("0.0") 
    @DecimalMax("1.0")
    private Double topP;

    @Min(0) 
    @Max(200)
    private Integer topK;

    @Min(1) 
    @Max(2048) // Tope seguro para evitar ataques de agotamiento de recursos
    private Integer numPredict;

    private Integer seed;

    // Punto 3: Parámetros de plantilla de prompt
    private String templateId; // ej: "conciso", "tutor", "extractor", "default"
    private String rol;        // ej: "tutor", "experto", "asistente"
    private String dominio;    // ej: "ingenieria de software", "general"
    private String idioma;     // ej: "espanol", "ingles"

    public ChatRequest() {}

    public ChatRequest(String sessionId, String question) {
        this.sessionId = sessionId;
        this.question = question;
    }

    // Getters y Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getTopP() { return topP; }
    public void setTopP(Double topP) { this.topP = topP; }

    public Integer getTopK() { return topK; }
    public void setTopK(Integer topK) { this.topK = topK; }

    public Integer getNumPredict() { return numPredict; }
    public void setNumPredict(Integer numPredict) { this.numPredict = numPredict; }

    public Integer getSeed() { return seed; }
    public void setSeed(Integer seed) { this.seed = seed; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getDominio() { return dominio; }
    public void setDominio(String dominio) { this.dominio = dominio; }

    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
}