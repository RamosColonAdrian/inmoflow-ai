package com.inmoflow.backend.property.application;

import com.inmoflow.backend.property.domain.OperationType;
import com.inmoflow.backend.property.domain.PropertyType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePropertyCommand(
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
        Boolean available
) {
}
