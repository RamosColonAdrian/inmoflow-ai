package com.inmoflow.backend.ai.infrastructure;

import com.inmoflow.backend.ai.application.AiResponseGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OllamaAiResponseGenerator implements AiResponseGenerator {

    private static final String SYSTEM_PROMPT = """
            Actua como un asistente inmobiliario.
            Responde siempre en espanol.
            Se breve y profesional.
            Si el lead quiere visitar un inmueble, pregunta por su disponibilidad para una visita.
            No inventes detalles del inmueble.
            Si falta informacion, haz una pregunta breve de seguimiento.
            """;

    private final OllamaClient ollamaClient;
    private final String model;

    public OllamaAiResponseGenerator(
            OllamaClient ollamaClient,
            @Value("${ollama.model:llama3.2}") String model
    ) {
        this.ollamaClient = ollamaClient;
        this.model = model;
    }

    @Override
    public String generateResponse(String leadMessage) {
        OllamaChatRequest request = new OllamaChatRequest(
                model,
                false,
                List.of(
                        new OllamaChatMessage("system", SYSTEM_PROMPT),
                        new OllamaChatMessage("user", leadMessage)
                )
        );

        OllamaChatResponse response = ollamaClient.chat(request);
        if (response == null || response.message() == null || response.message().content() == null) {
            throw new IllegalStateException("Ollama returned an invalid response");
        }

        String content = response.message().content().trim();
        if (content.isBlank()) {
            throw new IllegalStateException("Ollama returned an empty response");
        }

        return content;
    }
}
