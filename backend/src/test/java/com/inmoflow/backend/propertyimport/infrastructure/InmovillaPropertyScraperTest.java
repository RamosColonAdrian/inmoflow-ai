package com.inmoflow.backend.propertyimport.infrastructure;

import com.inmoflow.backend.propertyimport.application.PropertyScrapeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InmovillaPropertyScraperTest {

    @Mock
    private HttpPageFetcher httpPageFetcher;

    private final InmovillaPropertyParser parser = new InmovillaPropertyParser();

    private InmovillaPropertyScraper scraper;

    @BeforeEach
    void setUp() {
        scraper = new InmovillaPropertyScraper(httpPageFetcher, parser);
    }

    @Test
    void rootUrlGeneratesHomeRentalAndSaleUrls() {
        assertThat(scraper.buildListingUrls("https://www.grupoezeda.com")).containsExactly(
                "https://www.grupoezeda.com/",
                "https://www.grupoezeda.com/rental/",
                "https://www.grupoezeda.com/for-sale/"
        );

        assertThat(scraper.buildListingUrls("https://www.grupoezeda.com/")).containsExactly(
                "https://www.grupoezeda.com/",
                "https://www.grupoezeda.com/rental/",
                "https://www.grupoezeda.com/for-sale/"
        );
    }

    @Test
    void rentalUrlDoesNotGenerateDuplicatedRentalPath() {
        assertThat(scraper.buildListingUrls("https://www.grupoezeda.com/rental/")).containsExactly(
                "https://www.grupoezeda.com/rental/"
        );

        assertThat(scraper.buildListingUrls("https://www.grupoezeda.com/rental")).containsExactly(
                "https://www.grupoezeda.com/rental/"
        );
    }

    @Test
    void saleUrlDoesNotGenerateDuplicatedSalePath() {
        assertThat(scraper.buildListingUrls("https://www.grupoezeda.com/for-sale/")).containsExactly(
                "https://www.grupoezeda.com/for-sale/"
        );

        assertThat(scraper.buildListingUrls("https://www.grupoezeda.com/for-sale")).containsExactly(
                "https://www.grupoezeda.com/for-sale/"
        );
    }

    @Test
    void discoversPaginationLinksAndIgnoresDuplicatedOrUnsafeLinks() {
        String html = """
                <a href="/rental/?p=2">2</a>
                <a href="/rental/?p=2">Duplicate</a>
                <a href="/rental/pag/3/">3</a>
                <a href="/for-sale/page/2/">Sale page 2</a>
                <a href="/privacy-policy/">Privacy</a>
                <a href="/contact/">Contact</a>
                <a href="/rental/ficha/flat/sevilla/centro/1">Detail</a>
                <a href="https://external.example/rental/?p=2">External</a>
                """;

        List<String> links = scraper.discoverPaginationLinks(
                "https://www.grupoezeda.com/rental/",
                html,
                scraper.buildListingUrls("https://www.grupoezeda.com/")
        );

        assertThat(links).containsExactly(
                "https://www.grupoezeda.com/rental/?p=2",
                "https://www.grupoezeda.com/rental/pag/3/",
                "https://www.grupoezeda.com/for-sale/page/2/"
        );
    }

    @Test
    void ignoresStaticAssetsAsPaginationCandidatesEvenWhenNamesContainPagination() {
        String html = """
                <a href="/css/modulo-paginacionmapa-1.css/?x=123">Map pagination CSS</a>
                <a href="/css/modulo-paginacion-1.css/?x=123">Pagination CSS</a>
                <a href="/css/componentes/paginacion-1.css/">Component pagination CSS</a>
                <a href="/rental/favicon.png/">Favicon</a>
                <a href="/rental/css/reset.css/?x=123">Reset CSS</a>
                <a href="/rental/js/pagination.js">Pagination JS</a>
                <a href="/rental/img/page-2.png">Page image</a>
                <a href="/rental/images/paginacion.webp">Pagination image</a>
                <a href="/rental/fonts/site.woff2">Font</a>
                <a href="/rental/?p=2">Valid page</a>
                """;

        List<String> links = scraper.discoverPaginationLinks(
                "https://www.grupoezeda.com/rental/",
                html,
                scraper.buildListingUrls("https://www.grupoezeda.com/")
        );

        assertThat(links).containsExactly("https://www.grupoezeda.com/rental/?p=2");
    }

    @Test
    void ignoresLanguageNavigationAndActionListingCandidates() {
        String html = """
                <a href="/rental/gl/">Galician rental</a>
                <a href="/for-sale/gl/">Galician sale</a>
                <a href="/rental/en/">English rental nav</a>
                <a href="/for-sale/en/">English sale nav</a>
                <a href="/rental/index.php/">Rental index</a>
                <a href="/for-sale/index.php/">Sale index</a>
                <a href="/rental/rental/">Duplicated rental</a>
                <a href="/for-sale/rental/">Mixed sale rental</a>
                <a href="/promotions/">Promotions</a>
                <a href="/publica-tu-inmueble/">Publish property</a>
                <a href="/rental/?vistas=1">Views action</a>
                <a href="/rental/?descartadas=1">Discarded action</a>
                <a href="/rental/?favoritas=1">Favorites action</a>
                <a href="/rental/?p=2">Valid page</a>
                """;

        List<String> links = scraper.discoverPaginationLinks(
                "https://www.grupoezeda.com/rental/",
                html,
                scraper.buildListingUrls("https://www.grupoezeda.com/")
        );

        assertThat(links).containsExactly("https://www.grupoezeda.com/rental/?p=2");
    }

    @Test
    void deduplicatesLinksFromMultipleListingPagesAndRespectsMaxPropertiesGlobally() {
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/")).thenReturn("""
                <a href="/ficha/home-only/en/">Home only</a>
                <a href="/ficha/shared/en/">Shared</a>
                """);
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/rental/")).thenReturn("""
                <a href="/ficha/rental-only/en/">Rental only</a>
                <a href="/ficha/shared/en/">Shared duplicate</a>
                """);
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/for-sale/")).thenReturn("""
                <a href="/ficha/sale-only/en/">Sale only</a>
                <a href="/ficha/shared/en/">Shared duplicate</a>
                """);
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/ficha/home-only/en/")).thenReturn(detailHtml("HOME"));
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/ficha/shared/en/")).thenReturn(detailHtml("SHARED"));
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/ficha/rental-only/en/")).thenReturn(detailHtml("RENTAL"));

        PropertyScrapeResult result = scraper.scrape("https://www.grupoezeda.com/", 3);

        assertThat(result.listingUrlsInspected()).containsExactly(
                "https://www.grupoezeda.com/",
                "https://www.grupoezeda.com/rental/",
                "https://www.grupoezeda.com/for-sale/"
        );
        assertThat(result.discoveredLinksCount()).isEqualTo(4);
        assertThat(result.processedLinksCount()).isEqualTo(3);
        assertThat(result.properties())
                .extracting("reference")
                .containsExactly("HOME", "SHARED", "RENTAL");
        assertThat(result.sampleDiscoveredLinks()).contains(
                "https://www.grupoezeda.com/ficha/home-only/en/",
                "https://www.grupoezeda.com/ficha/rental-only/en/",
                "https://www.grupoezeda.com/ficha/sale-only/en/"
        );
        verify(httpPageFetcher, never()).fetch("https://www.grupoezeda.com/ficha/sale-only/en/");
    }

    @Test
    void inspectsPaginationPagesAndAppliesMaxPropertiesAfterGlobalDeduplication() {
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/")).thenReturn("""
                Pag 1/1 - Total 1 Properties
                <a href="/ficha/home-one/en/">Home one</a>
                """);
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/rental/")).thenReturn("""
                Pag 1/2 - Total 3 Properties
                <a href="/ficha/rental-one/en/">Rental one</a>
                <a href="/rental/?p=2">2</a>
                <a href="/contact/">Contact</a>
                """);
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/for-sale/")).thenReturn("""
                Pag 1/2 - Total 3 Properties
                <a href="/ficha/sale-one/en/">Sale one</a>
                <a href="/for-sale/page/2/">2</a>
                <a href="/privacy-policy/">Privacy</a>
                """);
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/rental/?p=2")).thenReturn("""
                <a href="/rental/ficha/flat/sevilla/centro/3433/29020075/en/">Rental duplicate normalized</a>
                <a href="/ficha/rental-two/en/">Rental two</a>
                """);
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/for-sale/page/2/")).thenReturn("""
                <a href="/ficha/sale-two/en/">Sale two</a>
                <a href="/ficha/rental-one/en/">Duplicate across pages</a>
                """);
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/ficha/home-one/en/")).thenReturn(detailHtml("HOME"));
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/ficha/rental-one/en/")).thenReturn(detailHtml("RENTAL-ONE"));
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/ficha/sale-one/en/")).thenReturn(detailHtml("SALE-ONE"));
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/ficha/flat/sevilla/centro/3433/29020075/en/")).thenReturn(detailHtml("RENTAL-NORMALIZED"));

        PropertyScrapeResult result = scraper.scrape("https://www.grupoezeda.com/", 4);

        assertThat(result.listingUrlsInspected()).containsExactly(
                "https://www.grupoezeda.com/",
                "https://www.grupoezeda.com/rental/",
                "https://www.grupoezeda.com/for-sale/",
                "https://www.grupoezeda.com/rental/?p=2",
                "https://www.grupoezeda.com/for-sale/page/2/"
        );
        assertThat(result.discoveredLinksCount()).isEqualTo(6);
        assertThat(result.processedLinksCount()).isEqualTo(4);
        assertThat(result.properties())
                .extracting("reference")
                .containsExactly("HOME", "RENTAL-ONE", "SALE-ONE", "RENTAL-NORMALIZED");
        verify(httpPageFetcher, never()).fetch("https://www.grupoezeda.com/ficha/rental-two/en/");
        verify(httpPageFetcher, never()).fetch("https://www.grupoezeda.com/ficha/sale-two/en/");
    }

    @Test
    void doesNotInspectStaticAssetUrlsDiscoveredFromListingPage() {
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/")).thenReturn("""
                <a href="/css/modulo-paginacion-1.css/?x=123">Pagination CSS</a>
                <a href="/rental/favicon.png/">Favicon</a>
                <a href="/ficha/home-only/en/">Home only</a>
                """);
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/rental/")).thenReturn("");
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/for-sale/")).thenReturn("");
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/ficha/home-only/en/")).thenReturn(detailHtml("HOME"));

        PropertyScrapeResult result = scraper.scrape("https://www.grupoezeda.com/", 10);

        assertThat(result.listingUrlsInspected()).containsExactly(
                "https://www.grupoezeda.com/",
                "https://www.grupoezeda.com/rental/",
                "https://www.grupoezeda.com/for-sale/"
        );
        assertThat(result.listingUrlsInspected()).noneSatisfy(url ->
                assertThat(url).containsAnyOf("/css/", "favicon.png")
        );
        assertThat(result.warnings()).noneSatisfy(warning ->
                assertThat(warning).containsAnyOf("modulo-paginacion", "favicon.png")
        );
    }

    @Test
    void doesNotInspectIgnoredNavigationListingUrls() {
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/")).thenReturn("""
                <a href="/rental/gl/">Galician rental</a>
                <a href="/rental/en/">English rental nav</a>
                <a href="/rental/index.php/">Rental index</a>
                <a href="/rental/rental/">Duplicated rental</a>
                <a href="/for-sale/rental/">Mixed sale rental</a>
                <a href="/promotions/">Promotions</a>
                <a href="/publica-tu-inmueble/">Publish property</a>
                <a href="/rental/?vistas=1">Views action</a>
                <a href="/ficha/home-only/en/">Home only</a>
                """);
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/rental/")).thenReturn("");
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/for-sale/")).thenReturn("");
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/ficha/home-only/en/")).thenReturn(detailHtml("HOME"));

        PropertyScrapeResult result = scraper.scrape("https://www.grupoezeda.com/", 10);

        assertThat(result.listingUrlsInspected()).containsExactly(
                "https://www.grupoezeda.com/",
                "https://www.grupoezeda.com/rental/",
                "https://www.grupoezeda.com/for-sale/"
        );
    }

    @Test
    void ignoresGalicianDetailUrlsAndKeepsEnglishDetailUrls() {
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/")).thenReturn("""
                <a href="/ficha/tipo/sevilla/centro/3433/18262214/gl/">Galician type</a>
                <a href="/ficha/garaxe/sevilla/centro/3433/26913621/gl/">Galician garage</a>
                <a href="/ficha/flat/sevilla/centro/3433/29020075/en/">English flat</a>
                """);
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/rental/")).thenReturn("");
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/for-sale/")).thenReturn("");
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/ficha/flat/sevilla/centro/3433/29020075/en/")).thenReturn(detailHtml("ENGLISH"));

        PropertyScrapeResult result = scraper.scrape("https://www.grupoezeda.com/", 10);

        assertThat(result.sampleDiscoveredLinks()).containsExactly(
                "https://www.grupoezeda.com/ficha/flat/sevilla/centro/3433/29020075/en/"
        );
        assertThat(result.properties()).extracting("reference").containsExactly("ENGLISH");
        verify(httpPageFetcher, never()).fetch("https://www.grupoezeda.com/ficha/tipo/sevilla/centro/3433/18262214/gl/");
        verify(httpPageFetcher, never()).fetch("https://www.grupoezeda.com/ficha/garaxe/sevilla/centro/3433/26913621/gl/");
    }

    @Test
    void skipsParsedDetailWhenRequiredFieldsAreMissing() {
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/")).thenReturn("""
                <a href="/ficha/flat/sevilla/centro/3433/29020075/en/">English flat</a>
                """);
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/rental/")).thenReturn("");
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/for-sale/")).thenReturn("");
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/ficha/flat/sevilla/centro/3433/29020075/en/")).thenReturn("""
                <html><body>Unparseable page</body></html>
                """);

        PropertyScrapeResult result = scraper.scrape("https://www.grupoezeda.com/", 10);

        assertThat(result.properties()).isEmpty();
        assertThat(result.warnings()).containsExactly(
                "Skipped property detail URL because required fields could not be parsed: https://www.grupoezeda.com/ficha/flat/sevilla/centro/3433/29020075/en/"
        );
    }

    @Test
    void warnsWhenPaginationTextIsDetectedButNoPaginationLinkIsDiscovered() {
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/")).thenReturn("""
                Pag 1/3 - Total 28 Properties
                <a href="/ficha/home-only/en/">Home only</a>
                """);
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/rental/")).thenReturn("");
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/for-sale/")).thenReturn("");
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/ficha/home-only/en/")).thenReturn(detailHtml("HOME"));

        PropertyScrapeResult result = scraper.scrape("https://www.grupoezeda.com/", 10);

        assertThat(result.warnings()).anySatisfy(warning ->
                assertThat(warning).contains("Detected pagination text on https://www.grupoezeda.com/ (Pag 1/3)")
        );
    }

    @Test
    void continuesWhenOneListingUrlFails() {
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/")).thenReturn("""
                <a href="/ficha/home-only/en/">Home only</a>
                """);
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/rental/"))
                .thenThrow(new IllegalArgumentException("HTTP status: 500"));
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/for-sale/")).thenReturn("""
                <a href="/ficha/sale-only/en/">Sale only</a>
                """);
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/ficha/home-only/en/")).thenReturn(detailHtml("HOME"));
        when(httpPageFetcher.fetch("https://www.grupoezeda.com/ficha/sale-only/en/")).thenReturn(detailHtml("SALE"));

        PropertyScrapeResult result = scraper.scrape("https://www.grupoezeda.com/", 10);

        assertThat(result.discoveredLinksCount()).isEqualTo(2);
        assertThat(result.processedLinksCount()).isEqualTo(2);
        assertThat(result.properties())
                .extracting("reference")
                .containsExactly("HOME", "SALE");
        assertThat(result.warnings()).anySatisfy(warning ->
                assertThat(warning).contains("Could not fetch listing URL https://www.grupoezeda.com/rental/")
        );
    }

    private String detailHtml(String reference) {
        return """
                <html>
                  <body>
                    <h1>Flat - Sevilla (Centro)</h1>
                    <p>Propose a price</p>
                    <p>850 €/month</p>
                    <p>Before and After</p>
                    <p>Flat - Sevilla (Centro), Built Surface 76m2, 3 Bedrooms, 1 Bathrooms.</p>
                    <p>Property Features</p>
                    <p>Reference %s</p>
                    <p>Type of Operation Sale</p>
                    <p>Type of property Flat</p>
                    <p>Zone / City Centro / Sevilla</p>
                    <p>Built Surface 76 m2</p>
                    <p>Bedrooms 3</p>
                    <p>Bathrooms 1</p>
                    <p>Energy Efficiency Rating</p>
                  </body>
                </html>
                """.formatted(reference);
    }
}
