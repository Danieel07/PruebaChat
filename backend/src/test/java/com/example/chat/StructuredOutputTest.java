package com.example.chat;

import com.example.chat.dto.Clasificacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.converter.BeanOutputConverter;

import static org.junit.jupiter.api.Assertions.*;

public class StructuredOutputTest {

    @Test
    @DisplayName("Punto 3: BeanOutputConverter genera JsonSchema con required fields")
    void testJsonSchemaGeneration() {
        BeanOutputConverter<Clasificacion> converter = new BeanOutputConverter<>(Clasificacion.class);
        String schema = converter.getJsonSchema();

        assertNotNull(schema);
        assertTrue(schema.contains("categoria"));
        assertTrue(schema.contains("confianza"));
        assertTrue(schema.contains("justificacion"));
        assertTrue(schema.contains("required"));
    }

    @Test
    @DisplayName("Punto 3: BeanOutputConverter convierte JSON a Record Clasificacion")
    void testJsonConversionToRecord() {
        BeanOutputConverter<Clasificacion> converter = new BeanOutputConverter<>(Clasificacion.class);
        String json = """
                {
                    "categoria": "SOPORTE",
                    "confianza": 95,
                    "justificacion": "El usuario reporta problemas con el inicio de sesion"
                }
                """;

        Clasificacion result = converter.convert(json);

        assertNotNull(result);
        assertEquals("SOPORTE", result.categoria());
        assertEquals(95, result.confianza());
        assertEquals("El usuario reporta problemas con el inicio de sesion", result.justificacion());
    }
}
