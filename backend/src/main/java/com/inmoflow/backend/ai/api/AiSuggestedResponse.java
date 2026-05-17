package com.inmoflow.backend.ai.api;

import java.util.UUID;

public record AiSuggestedResponse(
        UUID conversationId,
        String suggestedResponse,
        String intent,
        double confidence,
        boolean requiresHuman
) {
}
