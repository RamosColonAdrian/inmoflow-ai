package com.inmoflow.backend.property.api.request;

import com.inmoflow.backend.property.domain.OperationType;
import com.inmoflow.backend.property.domain.PropertyType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePropertyRequest(

        @NotNull(message = "Agency id is required")
        UUID agencyId,

        @NotBlank(message = "Property reference is required")
        @Size(max = 80, message = "Property reference must not exceed 80 characters")
        String reference,

        @NotBlank(message = "Property title is required")
        @Size(max = 180, message = "Property title must not exceed 180 characters")
        String title,

        String description,

        @DecimalMin(value = "0.00", message = "Price must be greater than or equal to 0")
        BigDecimal price,

        @Size(max = 120, message = "City must not exceed 120 characters")
        String city,

        @Size(max = 120, message = "Zone must not exceed 120 characters")
        String zone,

        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,

        @Min(value = 0, message = "Rooms must be greater than or equal to 0")
        Integer rooms,

        @Min(value = 0, message = "Bathrooms must be greater than or equal to 0")
        Integer bathrooms,

        @Min(value = 0, message = "Square meters must be greater than or equal to 0")
        Integer squareMeters,

        @NotNull(message = "Property type is required")
        PropertyType propertyType,

        @NotNull(message = "Operation type is required")
        OperationType operationType,

        @NotNull(message = "Availability is required")
        Boolean available,

        @Size(max = 1000, message = "Source URL must not exceed 1000 characters")
        String sourceUrl,

        String qualificationRulesText
) {
}
