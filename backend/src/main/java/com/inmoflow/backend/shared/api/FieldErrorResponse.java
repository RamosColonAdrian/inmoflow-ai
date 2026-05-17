package com.inmoflow.backend.shared.api;

public record FieldErrorResponse(
        String field,
        String message
) {
}
