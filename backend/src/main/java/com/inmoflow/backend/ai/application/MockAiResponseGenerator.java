package com.inmoflow.backend.ai.application;

import org.springframework.stereotype.Component;

@Component
public class MockAiResponseGenerator implements AiResponseGenerator {

    private static final String MOCK_RESPONSE = "Hola, gracias por tu interes. Para ayudarte mejor, indicanos tu disponibilidad o un telefono/email de contacto. Un agente confirmara los detalles.";

    @Override
    public String generateResponse(String leadMessage) {
        return response();
    }

    public String response() {
        return MOCK_RESPONSE;
    }
}
