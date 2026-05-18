package com.inmoflow.backend.inbound.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record InboundLeadRequest(

        @NotNull(message = "Agency id is required")
        UUID agencyId,

        UUID propertyId,

        @NotBlank(message = "Source is required")
        String source,

        @NotBlank(message = "Lead name is required")
        @Size(max = 150, message = "Lead name must not exceed 150 characters")
        String name,

        @Email(message = "Email must be valid")
        @Size(max = 180, message = "Email must not exceed 180 characters")
        String email,

        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone,

        @NotBlank(message = "Message is required")
        String message
) {
}
