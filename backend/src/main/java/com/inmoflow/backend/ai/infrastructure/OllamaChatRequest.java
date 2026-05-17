package com.inmoflow.backend.ai.infrastructure;

import java.util.List;

public record OllamaChatRequest(
        String model,
        boolean stream,
        List<OllamaChatMessage> messages
) {
}
