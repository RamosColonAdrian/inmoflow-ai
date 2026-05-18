package com.inmoflow.backend.propertyimport.api;

import com.inmoflow.backend.propertyimport.application.PropertyScraperType;

import java.util.List;

public record ImportPropertiesResponse(
        PropertyScraperType scraperType,
        String baseUrl,
        int importedCount,
        int skippedCount,
        int discoveredLinksCount,
        int processedLinksCount,
        int fetchedBaseHtmlLength,
        int totalFetchedHtmlLength,
        List<String> listingUrlsInspected,
        List<String> sampleDiscoveredLinks,
        List<ImportedPropertyResponse> importedProperties,
        List<String> errors,
        List<String> warnings
) {
}
