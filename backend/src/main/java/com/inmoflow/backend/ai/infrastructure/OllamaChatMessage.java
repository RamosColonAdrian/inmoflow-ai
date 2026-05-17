package com.inmoflow.backend.ai.infrastructure;

public record OllamaChatMessage(
        String role,
        String content
) {
}
