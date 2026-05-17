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
    private final ControlledVisitResponseGenerator controlledVisitResponseGenerator;

    @Value("${ai.provider:mock}")
    private String aiProvider;

    @Override
    public String generateResponse(String leadMessage) {
        return generateResponse(new AiResponseContext(leadMessage, null, java.util.List.of()));
    }

    @Override
    public String generateResponse(AiResponseContext context) {
        return controlledVisitResponseGenerator.generateResponse(context)
                .orElseGet(() -> generateProviderResponse(context));
    }

    private String generateProviderResponse(AiResponseContext context) {
        if (OLLAMA_PROVIDER.equalsIgnoreCase(aiProvider)) {
            try {
                return ollamaAiResponseGenerator.generateResponse(context);
            } catch (RuntimeException exception) {
                return mockAiResponseGenerator.response();
            }
        }

        return mockAiResponseGenerator.response();
    }
}
