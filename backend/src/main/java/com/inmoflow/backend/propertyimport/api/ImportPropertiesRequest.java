package com.inmoflow.backend.propertyimport.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record ImportPropertiesRequest(
        @NotNull(message = "Agency id is required")
        UUID agencyId,

        @NotBlank(message = "Base URL is required")
        String baseUrl,

        @NotBlank(message = "Scraper type is required")
        @Pattern(regexp = "INMOVILLA|GENERIC_HTML", message = "Scraper type must be INMOVILLA or GENERIC_HTML")
        String scraperType,

        Integer maxProperties
) {
}
