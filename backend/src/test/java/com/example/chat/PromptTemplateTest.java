package com.example.chat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PromptTemplateTest {

    private final StTemplateRenderer templateRenderer = StTemplateRenderer.builder()
            .startDelimiterToken('<')
            .endDelimiterToken('>')
            .build();

    @Test
    @DisplayName("Punto 3: Carga y renderizado de plantilla conciso.st")
    void testConcisoTemplateRendering() {
        Resource resource = new ClassPathResource("prompts/conciso.st");
        PromptTemplate template = PromptTemplate.builder()
                .resource(resource)
                .renderer(templateRenderer)
                .build();

        String rendered = template.render(Map.of(
                "rol", "arquitecto de software",
                "dominio", "microservicios",
                "idioma", "español"
        ));

        assertTrue(rendered.contains("microservicios"));
        assertTrue(rendered.contains("arquitecto de software"));
        assertTrue(rendered.contains("español"));
        assertFalse(rendered.contains("<dominio>"));
        assertFalse(rendered.contains("<rol>"));
    }

    @Test
    @DisplayName("Punto 3: Carga y renderizado de plantilla tutor.st")
    void testTutorTemplateRendering() {
        Resource resource = new ClassPathResource("prompts/tutor.st");
        PromptTemplate template = PromptTemplate.builder()
                .resource(resource)
                .renderer(templateRenderer)
                .build();

        String rendered = template.render(Map.of(
                "rol", "profesor universitario",
                "dominio", "algoritmos",
                "idioma", "inglés"
        ));

        assertTrue(rendered.contains("algoritmos"));
        assertTrue(rendered.contains("profesor universitario"));
        assertTrue(rendered.contains("inglés"));
    }

    @Test
    @DisplayName("Punto 3: Carga y renderizado de plantilla extractor.st")
    void testExtractorTemplateRendering() {
        Resource resource = new ClassPathResource("prompts/extractor.st");
        PromptTemplate template = PromptTemplate.builder()
                .resource(resource)
                .renderer(templateRenderer)
                .build();

        String rendered = template.render(Map.of(
                "rol", "analista de datos",
                "dominio", "finanzas",
                "idioma", "español"
        ));

        assertTrue(rendered.contains("finanzas"));
        assertTrue(rendered.contains("analista de datos"));
    }

    @Test
    @DisplayName("Punto 3: Delimitadores < > no colisionan con llaves JSON { }")
    void testJsonDelimitersNoConflict() {
        String jsonPrompt = "Eres un asistente para el dominio <dominio>. Responde en formato: {\"status\": \"ok\", \"dominio\": \"<dominio>\"}";
        PromptTemplate template = PromptTemplate.builder()
                .template(jsonPrompt)
                .renderer(templateRenderer)
                .build();

        String rendered = template.render(Map.of("dominio", "ciberseguridad"));

        assertTrue(rendered.contains("{\"status\": \"ok\", \"dominio\": \"ciberseguridad\"}"));
    }
}
