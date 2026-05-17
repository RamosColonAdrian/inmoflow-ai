package com.inmoflow.backend.appointment.api.request;

import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentStatusRequest(

        @NotNull(message = "Status is required")
        String status
) {
}
