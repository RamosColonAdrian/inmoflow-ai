package com.inmoflow.backend.ai.application;

import com.inmoflow.backend.ai.infrastructure.OllamaAiResponseGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
public class ConfiguredAiResponseGenerator implements AiResponseGenerator {

    private static final String OLLAMA_PROVIDER = "ollama";

    private final OllamaAiResponseGenerator ollamaAiResponseGenerator;
    private final MockAiResponseGenerator mockAiResponseGenerator;

    @Value("${ai.provider:mock}")
    private String aiProvider;

    @Override
    public String generateResponse(String leadMessage) {
        if (OLLAMA_PROVIDER.equalsIgnoreCase(aiProvider)) {
            try {
                return ollamaAiResponseGenerator.generateResponse(leadMessage);
            } catch (RuntimeException exception) {
                return mockAiResponseGenerator.response();
            }
        }

        return mockAiResponseGenerator.response();
    }
}
