package com.inmoflow.backend.appointment.api.response;

import com.inmoflow.backend.appointment.domain.Appointment;
import com.inmoflow.backend.appointment.domain.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID agencyId,
        UUID leadId,
        UUID propertyId,
        UUID conversationId,
        String requestedDateText,
        AppointmentStatus status,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getAgencyId(),
                appointment.getLeadId(),
                appointment.getPropertyId(),
                appointment.getConversationId(),
                appointment.getRequestedDateText(),
                appointment.getStatus(),
                appointment.getNotes(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}
