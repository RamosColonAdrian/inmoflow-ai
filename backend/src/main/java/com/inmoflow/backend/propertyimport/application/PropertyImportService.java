package com.inmoflow.backend.propertyimport.application;

import com.inmoflow.backend.property.application.CreatePropertyCommand;
import com.inmoflow.backend.property.application.PropertyService;
import com.inmoflow.backend.property.domain.OperationType;
import com.inmoflow.backend.property.domain.Property;
import com.inmoflow.backend.property.domain.PropertyType;
import com.inmoflow.backend.property.infrastructure.PropertyRepository;
import com.inmoflow.backend.propertyimport.api.ImportedPropertyResponse;
import com.inmoflow.backend.propertyimport.api.ImportPropertiesRequest;
import com.inmoflow.backend.propertyimport.api.ImportPropertiesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyImportService {

    private static final int DEFAULT_MAX_PROPERTIES = 20;
    private static final int MAX_PROPERTIES_LIMIT = 100;

    private final PropertyScraperRegistry scraperRegistry;
    private final PropertyService propertyService;
    private final PropertyRepository propertyRepository;

    @Transactional
    public ImportPropertiesResponse importProperties(ImportPropertiesRequest request) {
        PropertyScraperType scraperType = parseScraperType(request.scraperType());
        int maxProperties = normalizeMaxProperties(request.maxProperties());
        PropertyScraper scraper = scraperRegistry.get(scraperType);

        List<String> errors = new ArrayList<>();
        PropertyScrapeResult scrapeResult;
        try {
            scrapeResult = scraper.scrape(request.baseUrl(), maxProperties);
        } catch (RuntimeException exception) {
            errors.add(exception.getMessage());
            scrapeResult = PropertyScrapeResult.empty();
        }

        int skippedCount = 0;
        List<ImportedPropertyResponse> importedProperties = new ArrayList<>();

        for (ImportedPropertyData data : scrapeResult.properties()) {
            if (!hasRequiredFields(data)) {
                skippedCount++;
                errors.add("Skipped property detail URL because required fields could not be parsed: " + data.sourceUrl());
                continue;
            }

            String reference = normalizeReference(data.reference());
            if (propertyRepository.findByAgencyIdAndReference(request.agencyId(), reference).isPresent()) {
                skippedCount++;
                continue;
            }

            try {
                Property property = propertyService.create(new CreatePropertyCommand(
                        request.agencyId(),
                        reference,
                        truncate(data.title().trim(), 180),
                        data.description(),
                        data.price(),
                        truncate(data.city(), 120),
                        truncate(data.zone(), 120),
                        truncate(data.address(), 255),
                        data.rooms(),
                        data.bathrooms(),
                        data.squareMeters(),
                        defaultValue(data.propertyType(), PropertyType.APARTMENT),
                        defaultValue(data.operationType(), OperationType.SALE),
                        defaultValue(data.available(), true),
                        truncate(data.sourceUrl(), 1000),
                        null
                ));
                importedProperties.add(ImportedPropertyResponse.from(property));
            } catch (RuntimeException exception) {
                skippedCount++;
                errors.add("Could not import property " + reference + ": " + exception.getMessage());
            }
        }

        return new ImportPropertiesResponse(
                scraperType,
                request.baseUrl(),
                importedProperties.size(),
                skippedCount,
                scrapeResult.discoveredLinksCount(),
                scrapeResult.processedLinksCount(),
                scrapeResult.fetchedBaseHtmlLength(),
                scrapeResult.totalFetchedHtmlLength(),
                scrapeResult.listingUrlsInspected(),
                scrapeResult.sampleDiscoveredLinks(),
                importedProperties,
                errors,
                scrapeResult.warnings()
        );
    }

    private PropertyScraperType parseScraperType(String scraperType) {
        try {
            return PropertyScraperType.valueOf(scraperType.trim().toUpperCase());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Unsupported property scraper type: " + scraperType);
        }
    }

    private int normalizeMaxProperties(Integer maxProperties) {
        if (maxProperties == null) {
            return DEFAULT_MAX_PROPERTIES;
        }
        return Math.min(Math.max(maxProperties, 1), MAX_PROPERTIES_LIMIT);
    }

    private String normalizeReference(String reference) {
        String normalized = reference.trim();
        return truncate(normalized.replaceAll("\\s+", "-"), 80);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private boolean hasRequiredFields(ImportedPropertyData data) {
        return data != null
                && hasText(data.reference())
                && hasText(data.title())
                && !"Imported property".equalsIgnoreCase(data.title().trim());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private <T> T defaultValue(T value, T fallback) {
        return value == null ? fallback : value;
    }
}
