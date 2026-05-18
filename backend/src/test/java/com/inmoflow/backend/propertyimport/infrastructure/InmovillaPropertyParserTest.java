package com.inmoflow.backend.propertyimport.infrastructure;

import com.inmoflow.backend.property.domain.OperationType;
import com.inmoflow.backend.property.domain.PropertyType;
import com.inmoflow.backend.propertyimport.application.ImportedPropertyData;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InmovillaPropertyParserTest {

    private final InmovillaPropertyParser parser = new InmovillaPropertyParser();

    @Test
    void extractsPropertyLinks() {
        String html = """
                <a href="/ficha/piso/sevilla/centro/123">Property</a>
                <a href="/ficha/piso/sevilla/centro/123/en/">Property English</a>
                <a href="https://example.com/ficha/piso/sevilla/centro/123/en/">Duplicate</a>
                <a href="ficha/piso/sevilla/nervion/789/en/?foo=1&amp;bar=2">Relative without slash</a>
                <a href="/not-a-property">Other</a>
                <a href="/ficha/casa/sevilla/este/456/en/">Property 2</a>
                """;

        List<String> links = parser.findPropertyLinks("https://example.com/", html, 10);

        assertThat(links).containsExactly(
                "https://example.com/ficha/piso/sevilla/centro/123/en/",
                "https://example.com/ficha/piso/sevilla/nervion/789/en/?foo=1&bar=2",
                "https://example.com/ficha/casa/sevilla/este/456/en/"
        );
    }

    @Test
    void normalizesSectionDetailLinks() {
        String html = """
                <a href="/rental/ficha/flat/sevilla/centro/3433/29020075/en/">Rental</a>
                <a href="/for-sale/ficha/house/sevilla/centro/3433/23502046/en/">Sale</a>
                <a href="/rental/en/ficha/studio/sevilla/centro/3433/18262214/en/">Rental English</a>
                <a href="/for-sale/en/ficha/flat/sevilla/centro/3433/29020075/en/">Sale English</a>
                <a href="/ficha/tipo/sevilla/centro/3433/18262214/gl/">Galician type</a>
                <a href="/ficha/garaxe/sevilla/centro/3433/26913621/gl/">Galician garage</a>
                """;

        List<String> links = parser.findPropertyLinks("https://www.grupoezeda.com/rental/", html, 10);

        assertThat(links).containsExactly(
                "https://www.grupoezeda.com/ficha/flat/sevilla/centro/3433/29020075/en/",
                "https://www.grupoezeda.com/ficha/house/sevilla/centro/3433/23502046/en/",
                "https://www.grupoezeda.com/ficha/studio/sevilla/centro/3433/18262214/en/"
        );
    }

    @Test
    void canonicalizesDetailUrlByKeepingOnlyFichaPathAfterHost() {
        assertThat(parser.normalizeDetailUrl("https://www.grupoezeda.com/rental/en/ficha/studio/sevilla/centro/3433/18262214/en/"))
                .isEqualTo("https://www.grupoezeda.com/ficha/studio/sevilla/centro/3433/18262214/en/");
        assertThat(parser.normalizeDetailUrl("https://www.grupoezeda.com/for-sale/en/ficha/flat/sevilla/centro/3433/29020075/en/"))
                .isEqualTo("https://www.grupoezeda.com/ficha/flat/sevilla/centro/3433/29020075/en/");
        assertThat(parser.normalizeDetailUrl("https://www.grupoezeda.com/en/ficha/house/sevilla/centro/3433/23502046/en/?foo=bar"))
                .isEqualTo("https://www.grupoezeda.com/ficha/house/sevilla/centro/3433/23502046/en/?foo=bar");
    }

    @Test
    void canonicalizesBeforeDeduplicatingDetailLinks() {
        String html = """
                <a href="/ficha/studio/sevilla/centro/3433/18262214/en/">Canonical</a>
                <a href="/rental/en/ficha/studio/sevilla/centro/3433/18262214/en/">Section duplicate</a>
                <a href="/for-sale/en/ficha/studio/sevilla/centro/3433/18262214/en/">Sale section duplicate</a>
                """;

        List<String> links = parser.findPropertyLinks("https://www.grupoezeda.com/rental/en/", html, 10);

        assertThat(links).containsExactly(
                "https://www.grupoezeda.com/ficha/studio/sevilla/centro/3433/18262214/en/"
        );
    }

    @Test
    void discoversFichaLinksOutsideHrefAttributes() {
        String html = """
                <script>
                  window.propertyUrl = "https://example.com/ficha/piso/sevilla/centro/123/en/";
                  window.relativePropertyUrl = "/ficha/casa/sevilla/este/456/en/";
                </script>
                """;

        List<String> links = parser.findPropertyLinks("https://example.com/", html, 10);

        assertThat(links).containsExactly(
                "https://example.com/ficha/piso/sevilla/centro/123/en/",
                "https://example.com/ficha/casa/sevilla/este/456/en/"
        );
    }

    @Test
    void limitsDiscoveredLinks() {
        String html = """
                <a href="/ficha/one/en/">One</a>
                <a href="/ficha/two/en/">Two</a>
                <a href="/ficha/three/en/">Three</a>
                """;

        List<String> links = parser.findPropertyLinks("https://example.com/", html, 2);

        assertThat(links).containsExactly(
                "https://example.com/ficha/one/en/",
                "https://example.com/ficha/two/en/"
        );
    }

    @Test
    void parsesGrupoEzedaFlatDetail() {
        String html = """
                Encontramos la casa de tus sueños
                Search
                Bedrooms 1
                Bathrooms 1
                Flat - Sevilla (Centro)
                Propose a price
                850 €/month
                Before and After
                Flat - Sevilla (Centro), Built Surface 76m2, 3 Bedrooms, 1 Bathrooms.
                Property Features
                Reference 3032026
                Type of Operation Rent
                Type of property Flat
                Zone / City Centro / Sevilla
                Built Surface 76 m2
                Bedrooms 3
                Bathrooms 1
                Energy Efficiency Rating
                Related
                Reference SHOULD_NOT_BE_USED
                """;

        ImportedPropertyData property = parser.parseDetailPage(html, "https://example.com/ficha/piso/3032026");

        assertThat(property.reference()).isEqualTo("3032026");
        assertThat(property.title()).isEqualTo("Flat - Sevilla (Centro)");
        assertThat(property.price()).isEqualByComparingTo(new BigDecimal("850"));
        assertThat(property.operationType()).isEqualTo(OperationType.RENT);
        assertThat(property.propertyType()).isEqualTo(PropertyType.APARTMENT);
        assertThat(property.zone()).isEqualTo("Centro");
        assertThat(property.city()).isEqualTo("Sevilla");
        assertThat(property.squareMeters()).isEqualTo(76);
        assertThat(property.rooms()).isEqualTo(3);
        assertThat(property.bathrooms()).isEqualTo(1);
        assertThat(property.description()).isEqualTo("Flat - Sevilla (Centro), Built Surface 76m2, 3 Bedrooms, 1 Bathrooms.");
        assertThat(property.sourceUrl()).isEqualTo("https://example.com/ficha/piso/3032026");
    }

    @Test
    void parsesGrupoEzedaHouseDetail() {
        String html = """
                House - Sevilla (Centro)
                Propose a price
                185.000 €
                Before and After
                House - Sevilla (Centro), Built Surface 76m2, Plot Surface 17m2, 3 Bedrooms, 1 Bathrooms.
                Property Features
                Reference 730025
                Type of Operation For sale
                Type of property House
                Zone / City Centro / Sevilla
                Built Surface 76 m2
                Bedrooms 3
                Bathrooms 1
                Energy Efficiency Rating
                """;

        ImportedPropertyData property = parser.parseDetailPage(html, "https://example.com/ficha/house/730025");

        assertThat(property.reference()).isEqualTo("730025");
        assertThat(property.title()).isEqualTo("House - Sevilla (Centro)");
        assertThat(property.price()).isEqualByComparingTo(new BigDecimal("185000"));
        assertThat(property.operationType()).isEqualTo(OperationType.SALE);
        assertThat(property.propertyType()).isEqualTo(PropertyType.HOUSE);
        assertThat(property.zone()).isEqualTo("Centro");
        assertThat(property.city()).isEqualTo("Sevilla");
        assertThat(property.squareMeters()).isEqualTo(76);
        assertThat(property.rooms()).isEqualTo(3);
        assertThat(property.bathrooms()).isEqualTo(1);
    }

    @Test
    void parsesGrupoEzedaGarageDetailWithMissingRoomsAsZero() {
        String html = """
                Garage - Sevilla (Centro)
                Propose a price
                42.000 €
                Before and After
                Garage - Sevilla (Centro), Built Surface 10m2.
                Property Features
                Reference 233622025
                Type of Operation For sale
                Type of property Garage
                Zone / City Centro / Sevilla
                Built Surface 10 m2
                Energy Efficiency Rating
                """;

        ImportedPropertyData property = parser.parseDetailPage(html, "https://example.com/ficha/garage/233622025");

        assertThat(property.reference()).isEqualTo("233622025");
        assertThat(property.title()).isEqualTo("Garage - Sevilla (Centro)");
        assertThat(property.price()).isEqualByComparingTo(new BigDecimal("42000"));
        assertThat(property.operationType()).isEqualTo(OperationType.SALE);
        assertThat(property.propertyType()).isEqualTo(PropertyType.GARAGE);
        assertThat(property.zone()).isEqualTo("Centro");
        assertThat(property.city()).isEqualTo("Sevilla");
        assertThat(property.squareMeters()).isEqualTo(10);
        assertThat(property.rooms()).isZero();
        assertThat(property.bathrooms()).isZero();
    }

    @Test
    void supportsFeatureValuesSplitAcrossLinesAndCleansMojibake() {
        String html = """
                Studio - Sevilla (NerviÃ³n)
                Propose a price
                1.200 â‚¬/month
                Before and After
                Studio - Sevilla (NerviÃ³n), Built Surface 40m2, 1 Bedrooms, 1 Bathrooms.
                Property Features
                Reference
                998877
                Type of Operation
                Rental
                Type of property
                Studio
                Zone / City
                NerviÃ³n / Sevilla
                Built Surface
                40 m2
                Bedrooms
                1
                Bathrooms
                1
                Contact us
                """;

        ImportedPropertyData property = parser.parseDetailPage(html, "https://example.com/rental/ficha/studio/998877");

        assertThat(property.reference()).isEqualTo("998877");
        assertThat(property.title()).isEqualTo("Studio - Sevilla (Nervión)");
        assertThat(property.price()).isEqualByComparingTo(new BigDecimal("1200"));
        assertThat(property.operationType()).isEqualTo(OperationType.RENT);
        assertThat(property.propertyType()).isEqualTo(PropertyType.APARTMENT);
        assertThat(property.zone()).isEqualTo("Nervión");
        assertThat(property.city()).isEqualTo("Sevilla");
        assertThat(property.squareMeters()).isEqualTo(40);
        assertThat(property.rooms()).isEqualTo(1);
        assertThat(property.bathrooms()).isEqualTo(1);
        assertThat(property.sourceUrl()).isEqualTo("https://example.com/ficha/studio/998877");
    }
}
