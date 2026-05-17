package com.inmoflow.backend.appointment.application;

import java.util.UUID;

public record CreateAppointmentCommand(
        UUID agencyId,
        UUID leadId,
        UUID propertyId,
        UUID conversationId,
        String requestedDateText,
        String notes
) {
}
