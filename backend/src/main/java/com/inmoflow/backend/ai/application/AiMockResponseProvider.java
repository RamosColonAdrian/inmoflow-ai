package com.inmoflow.backend.ai.application;

import org.springframework.stereotype.Component;

@Component
public class AiMockResponseProvider {

    private static final String MOCK_RESPONSE = "Hola, gracias por tu interes. El inmueble sigue disponible. \u00bfTe gustaria agendar una visita esta semana?";

    public String response() {
        return MOCK_RESPONSE;
    }
}
