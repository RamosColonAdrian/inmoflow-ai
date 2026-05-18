package com.inmoflow.backend.propertyimport.api;

import com.inmoflow.backend.property.domain.Property;

import java.util.UUID;

public record ImportedPropertyResponse(
        UUID propertyId,
        String reference,
        String title,
        String sourceUrl
) {

    public static ImportedPropertyResponse from(Property property) {
        return new ImportedPropertyResponse(
                property.getId(),
                property.getReference(),
                property.getTitle(),
                property.getSourceUrl()
        );
    }
}
