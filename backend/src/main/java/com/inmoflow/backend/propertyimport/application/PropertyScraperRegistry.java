package com.inmoflow.backend.propertyimport.application;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PropertyScraperRegistry {

    private final Map<PropertyScraperType, PropertyScraper> scrapers;

    public PropertyScraperRegistry(List<PropertyScraper> scrapers) {
        this.scrapers = new EnumMap<>(PropertyScraperType.class);
        for (PropertyScraper scraper : scrapers) {
            this.scrapers.put(scraper.type(), scraper);
        }
    }

    public PropertyScraper get(PropertyScraperType type) {
        PropertyScraper scraper = scrapers.get(type);
        if (scraper == null) {
            throw new IllegalArgumentException("Unsupported property scraper type: " + type);
        }
        return scraper;
    }
}
