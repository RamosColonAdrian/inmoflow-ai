package com.inmoflow.backend.appointment.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAppointmentRequest(

        @NotNull(message = "Agency id is required")
        UUID agencyId,

        @NotNull(message = "Lead id is required")
        UUID leadId,

        UUID propertyId,

        UUID conversationId,

        @Size(max = 255, message = "Requested date text must not exceed 255 characters")
        String requestedDateText,

        String notes
) {
}
