package com.inmoflow.backend.propertyimport.infrastructure;

import com.inmoflow.backend.propertyimport.application.ImportedPropertyData;
import com.inmoflow.backend.propertyimport.application.PropertyScraper;
import com.inmoflow.backend.propertyimport.application.PropertyScrapeResult;
import com.inmoflow.backend.propertyimport.application.PropertyScraperType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class InmovillaPropertyScraper implements PropertyScraper {

    private static final int MAX_LISTING_PAGES_INSPECTED = 20;
    private static final Pattern HREF_PATTERN = Pattern.compile("(?is)href\\s*=\\s*([\"'])(.*?)\\1");
    private static final Pattern PAGINATION_TEXT_PATTERN = Pattern.compile("(?i)\\bPag\\s+1\\s*/\\s*(\\d+)\\b");
    private static final Pattern NUMERIC_PAGE_PATH_PATTERN = Pattern.compile("(?i).*/\\d+/?$");
    private static final List<String> STATIC_PATH_SEGMENTS = List.of(
            "/css/",
            "/js/",
            "/img/",
            "/images/",
            "/fonts/"
    );
    private static final List<String> STATIC_FILE_EXTENSIONS = List.of(
            ".css",
            ".js",
            ".png",
            ".jpg",
            ".jpeg",
            ".gif",
            ".svg",
            ".webp",
            ".ico",
            ".woff",
            ".woff2",
            ".ttf",
            ".map"
    );
    private static final List<String> BLOCKED_LISTING_QUERY_PARAMETERS = List.of(
            "vistas",
            "descartadas",
            "favoritas"
    );

    private final HttpPageFetcher httpPageFetcher;
    private final InmovillaPropertyParser parser;

    @Override
    public PropertyScraperType type() {
        return PropertyScraperType.INMOVILLA;
    }

    @Override
    public PropertyScrapeResult scrape(String baseUrl, int maxProperties) {
        List<String> seedListingUrls = buildListingUrls(baseUrl);
        Set<String> pendingListingUrls = new LinkedHashSet<>(seedListingUrls);
        Set<String> inspectedListingUrls = new LinkedHashSet<>();
        List<ListingFetchResult> fetchedListings = new ArrayList<>();
        Set<String> discoveredLinks = new LinkedHashSet<>();
        List<String> warnings = new ArrayList<>();
        int fetchedBaseHtmlLength = 0;
        int totalFetchedHtmlLength = 0;

        while (!pendingListingUrls.isEmpty() && inspectedListingUrls.size() < MAX_LISTING_PAGES_INSPECTED) {
            String listingUrl = first(pendingListingUrls);
            pendingListingUrls.remove(listingUrl);
            if (!inspectedListingUrls.add(listingUrl)) {
                continue;
            }

            try {
                String listingHtml = httpPageFetcher.fetch(listingUrl);
                if (inspectedListingUrls.size() == 1) {
                    fetchedBaseHtmlLength = listingHtml.length();
                }
                totalFetchedHtmlLength += listingHtml.length();
                List<String> listingLinks = parser.findPropertyLinks(listingUrl, listingHtml, Integer.MAX_VALUE);
                fetchedListings.add(new ListingFetchResult(listingLinks));
                discoveredLinks.addAll(listingLinks);

                List<String> paginationLinks = discoverPaginationLinks(listingUrl, listingHtml, seedListingUrls);
                paginationLinks.stream()
                        .filter(link -> !inspectedListingUrls.contains(link))
                        .forEach(pendingListingUrls::add);

                paginationPageCount(listingHtml)
                        .filter(pageCount -> pageCount > 1 && paginationLinks.isEmpty())
                        .ifPresent(pageCount -> warnings.add("Detected pagination text on " + listingUrl + " (Pag 1/" + pageCount + ") but no pagination URLs were discovered."));
            } catch (RuntimeException exception) {
                warnings.add("Could not fetch listing URL " + listingUrl + ": " + exception.getMessage());
            }
        }

        if (!pendingListingUrls.isEmpty()) {
            warnings.add("Reached maximum listing pages inspected (" + MAX_LISTING_PAGES_INSPECTED + "); some pagination URLs were not inspected.");
        }

        List<String> allDiscoveredLinks = discoveredLinks.stream().toList();
        List<String> propertyLinks = allDiscoveredLinks.stream()
                .limit(maxProperties)
                .toList();
        List<ImportedPropertyData> properties = new ArrayList<>();

        log.info(
                "Inmovilla import discovery. baseUrl={}, listingUrls={}, fetchedBaseHtmlLength={}, totalFetchedHtmlLength={}, discoveredLinksCount={}, firstLinks={}",
                baseUrl,
                inspectedListingUrls,
                fetchedBaseHtmlLength,
                totalFetchedHtmlLength,
                discoveredLinks.size(),
                allDiscoveredLinks.stream().limit(5).toList()
        );

        if (discoveredLinks.isEmpty()) {
            warnings.add("No property detail links were discovered from inspected listing URLs.");
        }

        for (String propertyLink : propertyLinks) {
            try {
                String normalizedPropertyLink = parser.normalizeDetailUrl(propertyLink);
                if (!parser.isSupportedDetailUrl(normalizedPropertyLink)) {
                    continue;
                }
                String detailHtml = httpPageFetcher.fetch(normalizedPropertyLink);
                ImportedPropertyData property = parser.parseDetailPage(detailHtml, normalizedPropertyLink);
                if (hasRequiredFields(property)) {
                    properties.add(property);
                } else {
                    warnings.add("Skipped property detail URL because required fields could not be parsed: " + normalizedPropertyLink);
                }
            } catch (RuntimeException exception) {
                // Keep an import run useful when one public listing page fails.
                warnings.add("Could not process property detail URL " + propertyLink + ": " + exception.getMessage());
            }
        }

        return new PropertyScrapeResult(
                properties,
                discoveredLinks.size(),
                properties.size(),
                fetchedBaseHtmlLength,
                totalFetchedHtmlLength,
                inspectedListingUrls.stream().toList(),
                sampleLinks(fetchedListings, allDiscoveredLinks),
                warnings
        );
    }

    List<String> buildListingUrls(String baseUrl) {
        URI uri = URI.create(baseUrl.trim());
        String path = uri.getPath();
        if (path == null || path.isBlank() || "/".equals(path)) {
            String rootUrl = rootUrl(uri);
            return List.of(
                    rootUrl,
                    rootUrl + "rental/",
                    rootUrl + "for-sale/"
            );
        }
        return List.of(canonicalUrl(uri));
    }

    private String rootUrl(URI uri) {
        return URI.create(uri.getScheme() + "://" + uri.getAuthority() + "/").toString();
    }

    private String canonicalUrl(URI uri) {
        String path = uri.getPath();
        String normalizedPath = path.endsWith("/") ? path : path + "/";
        return URI.create(uri.getScheme() + "://" + uri.getAuthority() + normalizedPath).toString();
    }

    List<String> discoverPaginationLinks(String listingUrl, String html, List<String> seedListingUrls) {
        URI listingUri = URI.create(listingUrl);
        Set<String> links = new LinkedHashSet<>();
        Matcher matcher = HREF_PATTERN.matcher(html);
        while (matcher.find()) {
            String href = decodeHtml(matcher.group(2)).trim();
            if (href.isBlank() || href.startsWith("#") || href.toLowerCase(Locale.ROOT).startsWith("javascript:")) {
                continue;
            }

            try {
                URI candidateUri = listingUri.resolve(href);
                String candidate = canonicalListingCandidate(candidateUri);
                if (isSafeListingPaginationUrl(listingUri, candidate, seedListingUrls)) {
                    links.add(candidate);
                }
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed public links and keep scanning.
            }
        }
        return links.stream().toList();
    }

    private boolean isSafeListingPaginationUrl(URI currentListingUri, String candidate, List<String> seedListingUrls) {
        URI candidateUri = URI.create(candidate);
        String currentHost = currentListingUri.getHost();
        if (currentHost == null || candidateUri.getHost() == null || !currentHost.equalsIgnoreCase(candidateUri.getHost())) {
            return false;
        }

        String path = normalizedPath(candidateUri);
        String lowerCandidate = candidate.toLowerCase(Locale.ROOT);
        if (lowerCandidate.contains("/ficha/")
                || isStaticAssetUrl(candidateUri)
                || isBlockedListingNavigationUrl(candidateUri)
                || isBlockedListingPath(lowerCandidate)) {
            return false;
        }

        return seedListingUrls.contains(candidate)
                || looksLikePagination(candidateUri)
                || isInsideKnownListingSection(path, seedListingUrls);
    }

    private boolean isInsideKnownListingSection(String path, List<String> seedListingUrls) {
        for (String seedListingUrl : seedListingUrls) {
            String seedPath = normalizedPath(URI.create(seedListingUrl));
            if ("/".equals(seedPath)) {
                continue;
            }
            if (path.startsWith(seedPath)) {
                return true;
            }
        }
        return false;
    }

    private boolean looksLikePagination(URI uri) {
        String value = uri.toString().toLowerCase(Locale.ROOT);
        String query = uri.getQuery();
        return value.contains("pag")
                || value.contains("page")
                || value.contains("pagina")
                || (query != null && query.toLowerCase(Locale.ROOT).contains("p="))
                || NUMERIC_PAGE_PATH_PATTERN.matcher(normalizedPath(uri)).matches();
    }

    private boolean isStaticAssetUrl(URI uri) {
        String lowerPath = normalizedPath(uri).toLowerCase(Locale.ROOT);
        if (STATIC_PATH_SEGMENTS.stream().anyMatch(lowerPath::contains)) {
            return true;
        }

        String pathWithoutTrailingSlashes = lowerPath.replaceAll("/+$", "");
        if (pathWithoutTrailingSlashes.endsWith("/favicon.png")) {
            return true;
        }

        return STATIC_FILE_EXTENSIONS.stream()
                .anyMatch(pathWithoutTrailingSlashes::endsWith);
    }

    private boolean isBlockedListingNavigationUrl(URI uri) {
        String lowerPath = normalizedPath(uri).toLowerCase(Locale.ROOT);
        String pathWithoutTrailingSlashes = lowerPath.replaceAll("/+$", "");
        String query = uri.getQuery();

        if (query != null) {
            String lowerQuery = query.toLowerCase(Locale.ROOT);
            if (BLOCKED_LISTING_QUERY_PARAMETERS.stream()
                    .anyMatch(parameter -> lowerQuery.contains(parameter + "="))) {
                return true;
            }
        }

        return pathWithoutTrailingSlashes.equals("/rental/gl")
                || pathWithoutTrailingSlashes.equals("/for-sale/gl")
                || pathWithoutTrailingSlashes.equals("/rental/en")
                || pathWithoutTrailingSlashes.equals("/for-sale/en")
                || pathWithoutTrailingSlashes.equals("/rental/index.php")
                || pathWithoutTrailingSlashes.equals("/for-sale/index.php")
                || pathWithoutTrailingSlashes.equals("/rental/for-sale")
                || pathWithoutTrailingSlashes.equals("/rental/rental")
                || pathWithoutTrailingSlashes.equals("/for-sale/for-sale")
                || pathWithoutTrailingSlashes.equals("/for-sale/rental")
                || pathWithoutTrailingSlashes.equals("/promotions")
                || pathWithoutTrailingSlashes.equals("/publica-tu-inmueble");
    }

    private boolean isBlockedListingPath(String value) {
        return value.contains("contact")
                || value.contains("privacy")
                || value.contains("legal")
                || value.contains("cookie")
                || value.contains("alert")
                || value.contains("company")
                || value.contains("about")
                || value.contains("aviso")
                || value.contains("privacidad")
                || value.contains("cookies")
                || value.contains("empresa");
    }

    private String canonicalListingCandidate(URI uri) {
        String path = uri.getPath();
        if (path == null || path.isBlank()) {
            path = "/";
        }
        String normalizedPath = path.endsWith("/") ? path : path + "/";
        String query = uri.getQuery();
        String url = uri.getScheme() + "://" + uri.getAuthority() + normalizedPath;
        if (query != null && !query.isBlank()) {
            url += "?" + query;
        }
        return URI.create(url).toString();
    }

    private String normalizedPath(URI uri) {
        String path = uri.getPath();
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path.endsWith("/") ? path : path + "/";
    }

    private String first(Set<String> values) {
        return values.iterator().next();
    }

    private Optional<Integer> paginationPageCount(String html) {
        Matcher matcher = PAGINATION_TEXT_PATTERN.matcher(parser.visibleText(html));
        if (!matcher.find()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(matcher.group(1)));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private String decodeHtml(String value) {
        return value.replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">");
    }

    private List<String> sampleLinks(List<ListingFetchResult> fetchedListings, List<String> allDiscoveredLinks) {
        Set<String> sample = new LinkedHashSet<>();
        for (ListingFetchResult listing : fetchedListings) {
            listing.discoveredLinks().stream()
                    .filter(allDiscoveredLinks::contains)
                    .findFirst()
                    .ifPresent(sample::add);
        }

        for (String link : allDiscoveredLinks) {
            if (sample.size() >= 10) {
                break;
            }
            sample.add(link);
        }
        return sample.stream().limit(10).toList();
    }

    private boolean hasRequiredFields(ImportedPropertyData property) {
        return hasText(property.reference())
                && hasText(property.title())
                && !"Imported property".equalsIgnoreCase(property.title().trim());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record ListingFetchResult(List<String> discoveredLinks) {
    }
}
