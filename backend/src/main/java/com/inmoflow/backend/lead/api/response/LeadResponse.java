package com.inmoflow.backend.lead.api.response;

import com.inmoflow.backend.lead.domain.Lead;
import com.inmoflow.backend.lead.domain.LeadSource;
import com.inmoflow.backend.lead.domain.LeadStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LeadResponse(
        UUID id,
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
        Integer score,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static LeadResponse from(Lead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getAgencyId(),
                lead.getPropertyId(),
                lead.getName(),
                lead.getEmail(),
                lead.getPhone(),
                lead.getSource(),
                lead.getStatus(),
                lead.getMessage(),
                lead.getBudget(),
                lead.getDesiredZone(),
                lead.getScore(),
                lead.getCreatedAt(),
                lead.getUpdatedAt()
        );
    }
}
