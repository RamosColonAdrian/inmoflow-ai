package com.inmoflow.backend.lead.application;

import com.inmoflow.backend.lead.domain.LeadSource;
import com.inmoflow.backend.lead.domain.LeadStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateLeadCommand(
        UUID agencyId,
        UUID propertyId,
        String name,
        String email,
        String phone,
        LeadSource source,
        LeadStatus status,
        String message,
        BigDecimal budget,
        String desiredZone,
        Integer score
) {
}
