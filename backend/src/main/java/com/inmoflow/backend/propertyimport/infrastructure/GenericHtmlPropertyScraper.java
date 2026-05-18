package com.inmoflow.backend.propertyimport.infrastructure;

import com.inmoflow.backend.propertyimport.application.PropertyScraper;
import com.inmoflow.backend.propertyimport.application.PropertyScrapeResult;
import com.inmoflow.backend.propertyimport.application.PropertyScraperType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GenericHtmlPropertyScraper implements PropertyScraper {

    @Override
    public PropertyScraperType type() {
        return PropertyScraperType.GENERIC_HTML;
    }

    @Override
    public PropertyScrapeResult scrape(String baseUrl, int maxProperties) {
        return new PropertyScrapeResult(
                List.of(),
                0,
                0,
                0,
                0,
                List.of(),
                List.of(),
                List.of("GENERIC_HTML scraper is a placeholder and does not import properties yet.")
        );
    }
}
