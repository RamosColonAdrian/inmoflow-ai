package com.inmoflow.backend.property.api.request;

import jakarta.validation.constraints.Size;

public record UpdateQualificationRulesRequest(
        @Size(max = 5000, message = "Qualification rules text must be 5000 characters or less")
        String qualificationRulesText
) {
}
