package com.example.chat.impl;

import com.example.chat.ModelService;
import com.example.chat.dto.ChatRequest;
import com.example.chat.dto.ChatResponse;
import com.example.chat.dto.Clasificacion;
import com.example.chat.dto.ClassifyRequest;
import com.example.chat.dto.ClassifyResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class SpringAiModelService implements ModelService {

    private final ChatClient chatClient;
    private final StTemplateRenderer templateRenderer;
    private final BeanOutputConverter<Clasificacion> clasificacionConverter;

    @Value("classpath:/prompts/conciso.st")
    private Resource concisoPrompt;

    @Value("classpath:/prompts/tutor.st")
    private Resource tutorPrompt;

    @Value("classpath:/prompts/extractor.st")
    private Resource extractorPrompt;

    @Value("classpath:/prompts/default.st")
    private Resource defaultPrompt;

    public SpringAiModelService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        // Configuración de delimitadores < y > para evitar conflicto con llaves JSON { }
        this.templateRenderer = StTemplateRenderer.builder()
                .startDelimiterToken('<')
                .endDelimiterToken('>')
                .build();
        this.clasificacionConverter = new BeanOutputConverter<>(Clasificacion.class);
    }

    private Resource resolvePromptTemplate(String templateId) {
        if (templateId == null) {
            return defaultPrompt;
        }
        return switch (templateId.trim().toLowerCase()) {
            case "conciso" -> concisoPrompt;
            case "tutor" -> tutorPrompt;
            case "extractor" -> extractorPrompt;
            default -> defaultPrompt;
        };
    }

    private String buildSystemPrompt(ChatRequest request) {
        Resource templateResource = resolvePromptTemplate(request.getTemplateId());
        String rol = request.getRol() != null && !request.getRol().isBlank() ? request.getRol() : "asistente";
        String dominio = request.getDominio() != null && !request.getDominio().isBlank() ? request.getDominio() : "general";
        String idioma = request.getIdioma() != null && !request.getIdioma().isBlank() ? request.getIdioma() : "español";

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .resource(templateResource)
                .renderer(templateRenderer)
                .build();

        return promptTemplate.render(Map.of(
                "rol", rol,
                "dominio", dominio,
                "idioma", idioma
        ));
    }

    private OllamaChatOptions.Builder buildOptionsBuilder(ChatRequest request) {
        var builder = OllamaChatOptions.builder();
        if (request.getTemperature() != null) { builder.temperature(request.getTemperature()); }
        if (request.getTopP() != null) { builder.topP(request.getTopP()); }
        if (request.getTopK() != null) { builder.topK(request.getTopK()); }
        if (request.getNumPredict() != null) { builder.numPredict(request.getNumPredict()); }
        if (request.getSeed() != null) { builder.seed(request.getSeed()); }
        return builder;
    }

    @Override
    public ChatResponse generateResponse(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        String requestId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();

        try {
            String systemText = buildSystemPrompt(request);
            var optionsBuilder = buildOptionsBuilder(request);

            org.springframework.ai.chat.model.ChatResponse aiResponse = chatClient.prompt()
                    .system(systemText)
                    .user(request.getQuestion() != null ? request.getQuestion() : "")
                    .options(optionsBuilder)
                    .call()
                    .chatResponse();

            long elapsedMs = System.currentTimeMillis() - startTime;
            String answer = (aiResponse != null && aiResponse.getResult() != null && aiResponse.getResult().getOutput() != null)
                    ? aiResponse.getResult().getOutput().getText() : "";

            Usage usage = aiResponse != null ? aiResponse.getMetadata().getUsage() : null;
            Integer promptTokens = usage != null ? usage.getPromptTokens() : 0;
            Integer completionTokens = usage != null ? usage.getCompletionTokens() : 0;
            Integer totalTokens = usage != null ? usage.getTotalTokens() : 0;

            String model = (aiResponse != null && aiResponse.getMetadata() != null)
                    ? aiResponse.getMetadata().getModel() : "unknown";
            String finishReason = (aiResponse != null && aiResponse.getResult() != null && aiResponse.getResult().getMetadata() != null)
                    ? aiResponse.getResult().getMetadata().getFinishReason() : "unknown";

            Double tokensPerSecond = 0.0;
            if (elapsedMs > 0 && completionTokens != null && completionTokens > 0) {
                tokensPerSecond = completionTokens / (elapsedMs / 1000.0);
            }

            return new ChatResponse(
                    requestId,
                    answer,
                    elapsedMs,
                    "ok",
                    promptTokens,
                    completionTokens,
                    totalTokens,
                    model,
                    finishReason,
                    tokensPerSecond
            );
        } catch (Exception e) {
            long elapsedMs = System.currentTimeMillis() - startTime;
            return new ChatResponse(
                    requestId,
                    "Error: " + e.getMessage(),
                    elapsedMs,
                    "error",
                    0, 0, 0, "error", "error", 0.0
            );
        }
    }

    @Override
    public ClassifyResponse classify(ClassifyRequest request) {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        String mode = request.getMode() != null ? request.getMode().trim().toUpperCase() : "C";

        ClassifyResponse response = new ClassifyResponse();
        response.setRequestId(requestId);
        response.setMode(mode);

        try {
            switch (mode) {
                case "A" -> executePromptInstructionMode(request, response, 0.2);
                case "B" -> executePromptInstructionMode(request, response, 1.0);
                case "C" -> executeEntityValidateSchemaMode(request, response);
                case "D" -> executeNativeOutputSchemaMode(request, response);
                default -> executeEntityValidateSchemaMode(request, response);
            }
            response.setStatus("ok");
        } catch (Exception e) {
            response.setStatus("error");
            response.setErrorMessage(e.getMessage());
            response.setParseSuccess(false);
        }

        long elapsedMs = System.currentTimeMillis() - startTime;
        response.setElapsedMs(elapsedMs);
        if (elapsedMs > 0 && response.getCompletionTokens() != null && response.getCompletionTokens() > 0) {
            response.setTokensPerSecond(response.getCompletionTokens() / (elapsedMs / 1000.0));
        }

        return response;
    }

    private void executePromptInstructionMode(ClassifyRequest request, ClassifyResponse response, double defaultTemp) {
        double temp = request.getTemperature() != null ? request.getTemperature() : defaultTemp;
        var optionsBuilder = OllamaChatOptions.builder().temperature(temp);

        String prompt = """
                Clasifica la siguiente consulta en formato JSON estricto con los siguientes campos:
                {
                  "categoria": "nombre de la categoría (ej: CONSULTA, RECLAMO, SOPORTE, INFORMACION, FACTURACION)",
                  "confianza": número entero entre 0 y 100 indicando nivel de confianza,
                  "justificacion": "breve justificación de la clasificación"
                }
                No incluyas explicaciones adicionales, ni markdown, ni comillas triples. Solo el JSON puro.
                Consulta: %s
                """.formatted(request.getText());

        org.springframework.ai.chat.model.ChatResponse aiResponse = chatClient.prompt()
                .user(prompt)
                .options(optionsBuilder)
                .call()
                .chatResponse();

        populateAiMetadata(aiResponse, response);

        String rawOutput = aiResponse != null && aiResponse.getResult() != null ? aiResponse.getResult().getOutput().getText() : "";
        response.setRawOutput(rawOutput);

        try {
            String cleanJson = extractJson(rawOutput);
            Clasificacion c = clasificacionConverter.convert(cleanJson);
            response.setClasificacion(c);
            response.setParseSuccess(true);
        } catch (Exception e) {
            response.setParseSuccess(false);
            response.setErrorMessage("Error parseando salida: " + e.getMessage());
        }
    }

    private void executeEntityValidateSchemaMode(ClassifyRequest request, ClassifyResponse response) {
        double temp = request.getTemperature() != null ? request.getTemperature() : 0.2;
        var optionsBuilder = OllamaChatOptions.builder().temperature(temp);

        String prompt = """
                Clasifica la consulta del usuario proporcionando la categoría, nivel de confianza (0-100) y justificación.
                Consulta: %s
                %s
                """.formatted(request.getText(), clasificacionConverter.getFormat());

        try {
            org.springframework.ai.chat.model.ChatResponse aiResponse = chatClient.prompt()
                    .user(prompt)
                    .options(optionsBuilder)
                    .call()
                    .chatResponse();

            populateAiMetadata(aiResponse, response);
            String rawOutput = aiResponse != null && aiResponse.getResult() != null ? aiResponse.getResult().getOutput().getText() : "";
            response.setRawOutput(rawOutput);

            Clasificacion c = clasificacionConverter.convert(extractJson(rawOutput));
            response.setClasificacion(c);
            response.setParseSuccess(true);
        } catch (Exception e) {
            response.setParseSuccess(false);
            response.setErrorMessage("Error en entity/validateSchema: " + e.getMessage());
        }
    }

    private void executeNativeOutputSchemaMode(ClassifyRequest request, ClassifyResponse response) {
        double temp = request.getTemperature() != null ? request.getTemperature() : 0.2;
        var optionsBuilder = OllamaChatOptions.builder()
                .temperature(temp)
                .outputSchema(clasificacionConverter.getJsonSchema());

        org.springframework.ai.chat.model.ChatResponse aiResponse = chatClient.prompt()
                .user("Clasifica esta consulta: " + request.getText())
                .options(optionsBuilder)
                .call()
                .chatResponse();

        populateAiMetadata(aiResponse, response);
        String rawOutput = aiResponse != null && aiResponse.getResult() != null ? aiResponse.getResult().getOutput().getText() : "";
        response.setRawOutput(rawOutput);

        try {
            Clasificacion c = clasificacionConverter.convert(extractJson(rawOutput));
            response.setClasificacion(c);
            response.setParseSuccess(true);
        } catch (Exception e) {
            response.setParseSuccess(false);
            response.setErrorMessage("Error convirtiendo esquema nativo: " + e.getMessage());
        }
    }

    private void populateAiMetadata(org.springframework.ai.chat.model.ChatResponse aiResponse, ClassifyResponse response) {
        if (aiResponse != null) {
            Usage usage = aiResponse.getMetadata() != null ? aiResponse.getMetadata().getUsage() : null;
            if (usage != null) {
                response.setPromptTokens(usage.getPromptTokens());
                response.setCompletionTokens(usage.getCompletionTokens());
                response.setTotalTokens(usage.getTotalTokens());
            }
            if (aiResponse.getMetadata() != null) {
                response.setModel(aiResponse.getMetadata().getModel());
            }
            if (aiResponse.getResult() != null && aiResponse.getResult().getMetadata() != null) {
                response.setFinishReason(aiResponse.getResult().getMetadata().getFinishReason());
            }
        }
    }

    private String extractJson(String text) {
        if (text == null) return "{}";
        String trimmed = text.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        trimmed = trimmed.trim();
        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace != -1 && lastBrace >= firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1);
        }
        return trimmed;
    }
}