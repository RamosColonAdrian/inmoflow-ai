package com.inmoflow.backend.propertyimport.application;

public interface PropertyScraper {

    PropertyScraperType type();

    PropertyScrapeResult scrape(String baseUrl, int maxProperties);
}
