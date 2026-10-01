package com.example.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Clasificacion(
        @JsonProperty(value = "categoria", required = true) String categoria,
        @JsonProperty(value = "confianza", required = true) int confianza,
        @JsonProperty(value = "justificacion", required = true) String justificacion
) {}
