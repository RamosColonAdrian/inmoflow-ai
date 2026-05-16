package com.inmoflow.backend.lead.api.request;

import com.inmoflow.backend.lead.domain.LeadSource;
import com.inmoflow.backend.lead.domain.LeadStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateLeadRequest(

        @NotNull(message = "Agency id is required")
        UUID agencyId,

        UUID propertyId,

        @Size(max = 150, message = "Lead name must not exceed 150 characters")
        String name,

        @Email(message = "Email must be valid")
        @Size(max = 180, message = "Email must not exceed 180 characters")
        String email,

        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone,

        @NotNull(message = "Lead source is required")
        LeadSource source,

        @NotNull(message = "Lead status is required")
        LeadStatus status,

        String message,

        @DecimalMin(value = "0.00", message = "Budget must be greater than or equal to 0")
        BigDecimal budget,

        @Size(max = 120, message = "Desired zone must not exceed 120 characters")
        String desiredZone,

        @Min(value = 0, message = "Score must be greater than or equal to 0")
        Integer score
) {
}
