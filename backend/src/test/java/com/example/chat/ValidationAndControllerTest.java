package com.example.chat;

import com.example.chat.dto.ChatRequest;
import com.example.chat.dto.ChatResponse;
import com.example.chat.dto.ClassifyRequest;
import com.example.chat.dto.ClassifyResponse;
import com.example.chat.dto.Clasificacion;
import com.example.chat.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ValidationAndControllerTest {

    private MockMvc mockMvc;
    private ModelService modelService;

    @BeforeEach
    void setUp() {
        modelService = Mockito.mock(ModelService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new ChatController(modelService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("Punto 2: Rechazar temperature fuera de rango (> 2.0) con HTTP 400")
    void testTemperatureValidationOutOfRange() throws Exception {
        String payload = """
                {
                    "sessionId": "test-session",
                    "question": "¿Qué es un token?",
                    "temperature": 17.0
                }
                """;

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.containsString("temperature")));
    }

    @Test
    @DisplayName("Punto 2: Rechazar topP fuera de rango (> 1.0) con HTTP 400")
    void testTopPValidationOutOfRange() throws Exception {
        String payload = """
                {
                    "sessionId": "test-session",
                    "question": "¿Qué es un token?",
                    "topP": 1.5
                }
                """;

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.containsString("topP")));
    }

    @Test
    @DisplayName("Punto 2: Rechazar topK fuera de rango (> 200) con HTTP 400")
    void testTopKValidationOutOfRange() throws Exception {
        String payload = """
                {
                    "sessionId": "test-session",
                    "question": "¿Qué es un token?",
                    "topK": 500
                }
                """;

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.containsString("topK")));
    }

    @Test
    @DisplayName("Punto 2: Rechazar numPredict fuera de rango (> 2048) con HTTP 400")
    void testNumPredictValidationOutOfRange() throws Exception {
        String payload = """
                {
                    "sessionId": "test-session",
                    "question": "¿Qué es un token?",
                    "numPredict": 5000
                }
                """;

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.containsString("numPredict")));
    }

    @Test
    @DisplayName("Punto 1 y 2: Petición válida debe responder con metadata de tokens completa")
    void testValidChatRequestWithSamplingAndTokenMetrics() throws Exception {
        ChatResponse mockResponse = new ChatResponse(
                "s1",
                "Un token es la unidad básica de procesamiento.",
                120,
                "ok",
                25,
                15,
                40,
                "llama3.2:3b",
                "stop",
                125.0
        );
        when(modelService.generateResponse(any(ChatRequest.class))).thenReturn(mockResponse);

        String payload = """
                {
                    "sessionId": "s1",
                    "question": "Explica en una frase qué es un token",
                    "temperature": 0.7,
                    "topP": 0.9,
                    "topK": 40,
                    "numPredict": 100,
                    "seed": 42,
                    "templateId": "conciso",
                    "rol": "experto en IA",
                    "dominio": "sistemas",
                    "idioma": "español"
                }
                """;

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("s1"))
                .andExpect(jsonPath("$.answer").value("Un token es la unidad básica de procesamiento."))
                .andExpect(jsonPath("$.promptTokens").value(25))
                .andExpect(jsonPath("$.completionTokens").value(15))
                .andExpect(jsonPath("$.totalTokens").value(40))
                .andExpect(jsonPath("$.model").value("llama3.2:3b"))
                .andExpect(jsonPath("$.finishReason").value("stop"))
                .andExpect(jsonPath("$.tokensPerSecond").value(125.0));
    }

    @Test
    @DisplayName("Punto 3: Endpoint /api/v1/chat/classify retorna salida estructurada")
    void testClassifyEndpoint() throws Exception {
        ClassifyResponse mockResponse = new ClassifyResponse();
        mockResponse.setRequestId("req-123");
        mockResponse.setMode("C");
        mockResponse.setStatus("ok");
        mockResponse.setClasificacion(new Clasificacion("SOPORTE", 90, "Problema técnico"));
        mockResponse.setPromptTokens(35);
        mockResponse.setCompletionTokens(20);
        mockResponse.setTotalTokens(55);
        mockResponse.setElapsedMs(150);
        mockResponse.setTokensPerSecond(133.3);
        mockResponse.setParseSuccess(true);

        when(modelService.classify(any(ClassifyRequest.class))).thenReturn(mockResponse);

        String payload = """
                {
                    "text": "Mi servicio no responde y da timeout",
                    "mode": "C",
                    "temperature": 0.2
                }
                """;

        mockMvc.perform(post("/api/v1/chat/classify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.mode").value("C"))
                .andExpect(jsonPath("$.clasificacion.categoria").value("SOPORTE"))
                .andExpect(jsonPath("$.clasificacion.confianza").value(90))
                .andExpect(jsonPath("$.clasificacion.justificacion").value("Problema técnico"))
                .andExpect(jsonPath("$.parseSuccess").value(true));
    }
}
