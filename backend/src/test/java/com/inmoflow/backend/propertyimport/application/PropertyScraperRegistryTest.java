package com.inmoflow.backend.propertyimport.application;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyScraperRegistryTest {

    @Test
    void selectsScraperByType() {
        PropertyScraper inmovillaScraper = new TestScraper(PropertyScraperType.INMOVILLA);
        PropertyScraper genericScraper = new TestScraper(PropertyScraperType.GENERIC_HTML);
        PropertyScraperRegistry registry = new PropertyScraperRegistry(List.of(inmovillaScraper, genericScraper));

        assertThat(registry.get(PropertyScraperType.INMOVILLA)).isSameAs(inmovillaScraper);
        assertThat(registry.get(PropertyScraperType.GENERIC_HTML)).isSameAs(genericScraper);
    }

    @Test
    void throwsCleanErrorForUnsupportedScraper() {
        PropertyScraperRegistry registry = new PropertyScraperRegistry(List.of());

        assertThatThrownBy(() -> registry.get(PropertyScraperType.INMOVILLA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported property scraper type");
    }

    private record TestScraper(PropertyScraperType type) implements PropertyScraper {

        @Override
        public PropertyScrapeResult scrape(String baseUrl, int maxProperties) {
            return PropertyScrapeResult.empty();
        }
    }
}
