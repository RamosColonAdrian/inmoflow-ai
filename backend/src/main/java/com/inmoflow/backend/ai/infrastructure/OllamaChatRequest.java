package com.inmoflow.backend.ai.infrastructure;

import java.util.List;
import java.util.Map;

public record OllamaChatRequest(
        String model,
        boolean stream,
        Map<String, Object> options,
        List<OllamaChatMessage> messages
) {
}
