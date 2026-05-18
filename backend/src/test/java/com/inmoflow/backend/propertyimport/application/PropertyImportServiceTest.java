package com.inmoflow.backend.propertyimport.application;

import com.inmoflow.backend.property.application.PropertyService;
import com.inmoflow.backend.property.domain.OperationType;
import com.inmoflow.backend.property.domain.Property;
import com.inmoflow.backend.property.domain.PropertyType;
import com.inmoflow.backend.property.infrastructure.PropertyRepository;
import com.inmoflow.backend.propertyimport.api.ImportPropertiesRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyImportServiceTest {

    @Mock
    private PropertyScraperRegistry scraperRegistry;

    @Mock
    private PropertyScraper scraper;

    @Mock
    private PropertyService propertyService;

    @Mock
    private PropertyRepository propertyRepository;

    private PropertyImportService service;

    @BeforeEach
    void setUp() {
        service = new PropertyImportService(scraperRegistry, propertyService, propertyRepository);
    }

    @Test
    void skipsInvalidParsedPropertiesWithoutGeneratingFallbackReferenceOrTitle() {
        UUID agencyId = UUID.randomUUID();
        ImportedPropertyData invalidProperty = new ImportedPropertyData(
                null,
                "Imported property",
                null,
                null,
                null,
                null,
                null,
                0,
                0,
                null,
                PropertyType.APARTMENT,
                OperationType.SALE,
                true,
                "https://www.grupoezeda.com/ficha/tipo/sevilla/centro/3433/18262214/gl/"
        );
        when(scraperRegistry.get(PropertyScraperType.INMOVILLA)).thenReturn(scraper);
        when(scraper.scrape("https://www.grupoezeda.com/", 10)).thenReturn(new PropertyScrapeResult(
                List.of(invalidProperty),
                1,
                0,
                100,
                100,
                List.of("https://www.grupoezeda.com/"),
                List.of(),
                List.of()
        ));

        var response = service.importProperties(new ImportPropertiesRequest(
                agencyId,
                "https://www.grupoezeda.com/",
                "INMOVILLA",
                10
        ));

        assertThat(response.importedCount()).isZero();
        assertThat(response.skippedCount()).isEqualTo(1);
        assertThat(response.errors()).containsExactly(
                "Skipped property detail URL because required fields could not be parsed: https://www.grupoezeda.com/ficha/tipo/sevilla/centro/3433/18262214/gl/"
        );
        verify(propertyRepository, never()).findByAgencyIdAndReference(any(), any());
        verify(propertyService, never()).create(any());
    }

    @Test
    void importsValidParsedPropertiesUsingRealReference() {
        UUID agencyId = UUID.randomUUID();
        ImportedPropertyData validProperty = new ImportedPropertyData(
                "3032026",
                "Flat - Sevilla (Centro)",
                "Flat - Sevilla (Centro), Built Surface 76m2, 3 Bedrooms, 1 Bathrooms.",
                null,
                "Sevilla",
                "Centro",
                null,
                3,
                1,
                76,
                PropertyType.APARTMENT,
                OperationType.RENT,
                true,
                "https://www.grupoezeda.com/ficha/flat/sevilla/centro/3433/3032026/en/"
        );
        Property savedProperty = Property.builder()
                .agencyId(agencyId)
                .reference("3032026")
                .title("Flat - Sevilla (Centro)")
                .propertyType(PropertyType.APARTMENT)
                .operationType(OperationType.RENT)
                .available(true)
                .sourceUrl(validProperty.sourceUrl())
                .build();

        when(scraperRegistry.get(PropertyScraperType.INMOVILLA)).thenReturn(scraper);
        when(scraper.scrape("https://www.grupoezeda.com/", 10)).thenReturn(new PropertyScrapeResult(
                List.of(validProperty),
                1,
                1,
                100,
                100,
                List.of("https://www.grupoezeda.com/"),
                List.of(validProperty.sourceUrl()),
                List.of()
        ));
        when(propertyRepository.findByAgencyIdAndReference(agencyId, "3032026")).thenReturn(Optional.empty());
        when(propertyService.create(any())).thenReturn(savedProperty);

        var response = service.importProperties(new ImportPropertiesRequest(
                agencyId,
                "https://www.grupoezeda.com/",
                "INMOVILLA",
                10
        ));

        assertThat(response.importedCount()).isEqualTo(1);
        assertThat(response.skippedCount()).isZero();
        verify(propertyRepository).findByAgencyIdAndReference(agencyId, "3032026");
    }
}
