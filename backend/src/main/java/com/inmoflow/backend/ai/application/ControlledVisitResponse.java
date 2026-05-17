package com.inmoflow.backend.ai.application;

public record ControlledVisitResponse(
        String response,
        String requestedDateText
) {
    public boolean hasRequestedDateText() {
        return requestedDateText != null && !requestedDateText.isBlank();
    }
}
