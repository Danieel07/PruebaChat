package com.example.chat.dto;

import jakarta.validation.constraints.NotBlank;

public class ClassifyRequest {

    @NotBlank(message = "El texto a clasificar no puede estar vacio")
    private String text;

    /**
     * Modos del Experimento D:
     * "A" -> Solo instruccion en prompt (temp 0.2)
     * "B" -> Solo instruccion en prompt (temp 1.0)
     * "C" -> entity(Clasificacion.class) con validateSchema() (temp 0.2)
     * "D" -> outputSchema() nativo de Ollama (temp 0.2)
     */
    private String mode = "C";

    private Double temperature;

    public ClassifyRequest() {}

    public ClassifyRequest(String text, String mode, Double temperature) {
        this.text = text;
        this.mode = mode;
        this.temperature = temperature;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
}
