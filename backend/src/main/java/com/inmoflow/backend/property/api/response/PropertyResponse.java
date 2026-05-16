package com.inmoflow.backend.property.api.response;

import com.inmoflow.backend.property.domain.OperationType;
import com.inmoflow.backend.property.domain.Property;
import com.inmoflow.backend.property.domain.PropertyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PropertyResponse(
        UUID id,
        UUID agencyId,
        String reference,
        String title,
        String description,
        BigDecimal price,
        String city,
        String zone,
        String address,
        Integer rooms,
        Integer bathrooms,
        Integer squareMeters,
        PropertyType propertyType,
        OperationType operationType,
        Boolean available,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PropertyResponse from(Property property) {
        return new PropertyResponse(
                property.getId(),
                property.getAgencyId(),
                property.getReference(),
                property.getTitle(),
                property.getDescription(),
                property.getPrice(),
                property.getCity(),
                property.getZone(),
                property.getAddress(),
                property.getRooms(),
                property.getBathrooms(),
                property.getSquareMeters(),
                property.getPropertyType(),
                property.getOperationType(),
                property.getAvailable(),
                property.getCreatedAt(),
                property.getUpdatedAt()
        );
    }
}
