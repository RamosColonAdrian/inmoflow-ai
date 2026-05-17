package com.inmoflow.backend.ai.application;

import org.springframework.stereotype.Component;

@Component
public class MockAiResponseGenerator implements AiResponseGenerator {

    private static final String MOCK_RESPONSE = "Hola, gracias por tu interes. El inmueble sigue disponible. \u00bfTe gustaria agendar una visita esta semana?";

    @Override
    public String generateResponse(String leadMessage) {
        return response();
    }

    public String response() {
        return MOCK_RESPONSE;
    }
}
