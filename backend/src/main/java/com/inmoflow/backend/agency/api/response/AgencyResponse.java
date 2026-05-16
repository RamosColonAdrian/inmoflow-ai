package com.inmoflow.backend.agency.api.response;

import com.inmoflow.backend.agency.domain.Agency;

import java.time.LocalDateTime;
import java.util.UUID;

public record AgencyResponse(
        UUID id,
        String name,
        String email,
        String phone,
        String website,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AgencyResponse from(Agency agency) {
        return new AgencyResponse(
                agency.getId(),
                agency.getName(),
                agency.getEmail(),
                agency.getPhone(),
                agency.getWebsite(),
                agency.getCreatedAt(),
                agency.getUpdatedAt()
        );
    }
}