package com.inmoflow.backend.inbound.api;

import com.inmoflow.backend.lead.domain.LeadSource;
import com.inmoflow.backend.lead.domain.LeadStatus;

import java.util.UUID;

public record InboundLeadResponse(
        UUID leadId,
        UUID conversationId,
        LeadStatus leadStatus,
        Integer leadScore,
        LeadSource source,
        String messageReceived,
        boolean appointmentCreated,
        boolean botResponseCreated
) {
}
