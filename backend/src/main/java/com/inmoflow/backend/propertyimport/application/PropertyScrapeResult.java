package com.inmoflow.backend.propertyimport.application;

import java.util.List;

public record PropertyScrapeResult(
        List<ImportedPropertyData> properties,
        int discoveredLinksCount,
        int processedLinksCount,
        int fetchedBaseHtmlLength,
        int totalFetchedHtmlLength,
        List<String> listingUrlsInspected,
        List<String> sampleDiscoveredLinks,
        List<String> warnings
) {

    public static PropertyScrapeResult empty() {
        return new PropertyScrapeResult(List.of(), 0, 0, 0, 0, List.of(), List.of(), List.of());
    }
}
