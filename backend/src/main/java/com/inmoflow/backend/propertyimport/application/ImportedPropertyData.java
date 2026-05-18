package com.inmoflow.backend.propertyimport.application;

import com.inmoflow.backend.property.domain.OperationType;
import com.inmoflow.backend.property.domain.PropertyType;

import java.math.BigDecimal;

public record ImportedPropertyData(
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
        String sourceUrl
) {
}
