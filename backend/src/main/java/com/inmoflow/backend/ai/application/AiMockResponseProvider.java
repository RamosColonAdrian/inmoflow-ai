package com.inmoflow.backend.ai.application;

import org.springframework.stereotype.Component;

@Component
public class AiMockResponseProvider {

    private final MockAiResponseGenerator mockAiResponseGenerator;

    public AiMockResponseProvider(MockAiResponseGenerator mockAiResponseGenerator) {
        this.mockAiResponseGenerator = mockAiResponseGenerator;
    }

    public String response() {
        return mockAiResponseGenerator.response();
    }
}
