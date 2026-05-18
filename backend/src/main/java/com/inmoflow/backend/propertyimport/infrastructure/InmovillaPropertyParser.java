package com.inmoflow.backend.propertyimport.infrastructure;

import com.inmoflow.backend.property.domain.OperationType;
import com.inmoflow.backend.property.domain.PropertyType;
import com.inmoflow.backend.propertyimport.application.ImportedPropertyData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class InmovillaPropertyParser {

    private static final Pattern HREF_PATTERN = Pattern.compile("(?is)href\\s*=\\s*([\"'])(.*?)\\1");
    private static final Pattern FALLBACK_FICHA_PATTERN = Pattern.compile("(?i)(https?://[^\\s\"'<>]+ficha[^\\s\"'<>]*|/[^\\s\"'<>]*ficha[^\\s\"'<>]*|(?:\\.\\./)?[^\\s\"'<>]*ficha/[^\\s\"'<>]*)");
    private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern PRICE_PATTERN = Pattern.compile("(?i)(\\d{1,3}(?:\\.\\d{3})*(?:,\\d{2})?|\\d+)\\s*(?:€|eur)(?:\\s*/\\s*(?:month|mes))?");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("\\d+");
    private static final Pattern PROPERTY_HEADING_PATTERN = Pattern.compile("(?i)^(Flat|Studio|House|Garage)\\s+-\\s+.+\\(.+\\)$");
    private static final List<String> FEATURE_STOP_MARKERS = List.of(
            "Energy Efficiency Rating",
            "Situation",
            "Contact us",
            "Related",
            "Mortgage simulator",
            "Subscribe"
    );
    private static final List<String> FOOTER_STOP_MARKERS = List.of(
            "Privacy Policy",
            "Legal Notice",
            "Cookies Policy",
            "Follow us"
    );

    public List<String> findPropertyLinks(String baseUrl, String html, int maxProperties) {
        URI baseUri = URI.create(baseUrl);
        Set<String> links = new LinkedHashSet<>();

        collectHrefLinks(baseUri, html, links);
        collectFallbackLinks(baseUri, html, links);

        return links.stream()
                .limit(maxProperties)
                .toList();
    }

    private void collectHrefLinks(URI baseUri, String html, Set<String> links) {
        Matcher matcher = HREF_PATTERN.matcher(html);
        while (matcher.find()) {
            String href = decodeHtml(matcher.group(2)).trim();
            if (containsFicha(href)) {
                addNormalizedLink(baseUri, href, links);
            }
        }
    }

    private void collectFallbackLinks(URI baseUri, String html, Set<String> links) {
        Matcher matcher = FALLBACK_FICHA_PATTERN.matcher(html);
        while (matcher.find()) {
            addNormalizedLink(baseUri, decodeHtml(matcher.group(1)).trim(), links);
        }
    }

    private void addNormalizedLink(URI baseUri, String rawLink, Set<String> links) {
        if (rawLink.isBlank() || rawLink.startsWith("#") || rawLink.toLowerCase(Locale.ROOT).startsWith("javascript:")) {
            return;
        }

        try {
            String normalizedLink = normalizeDetailUrl(baseUri.resolve(rawLink).toString());
            if (isSupportedDetailUrl(normalizedLink)) {
                links.add(normalizedLink);
            }
        } catch (IllegalArgumentException ignored) {
            // Ignore malformed public links and keep scanning.
        }
    }

    private boolean containsFicha(String value) {
        return value.toLowerCase(Locale.ROOT).contains("ficha");
    }

    public ImportedPropertyData parseDetailPage(String html, String sourceUrl) {
        String visibleText = visibleText(cleanMojibake(html));
        String featureSection = featureSection(visibleText);
        String reference = findFeature(featureSection, "Reference").orElse(null);
        String operation = findFeature(featureSection, "Type of Operation").orElse(null);
        String propertyType = findFeature(featureSection, "Type of property").orElse(null);
        ZoneCity zoneCity = parseZoneCity(findFeature(featureSection, "Zone / City").orElse(null));
        String title = titleFrom(visibleText, propertyType, zoneCity).orElse("Imported property");
        String description = descriptionFrom(visibleText).orElse(null);

        return new ImportedPropertyData(
                reference,
                truncate(title, 180),
                truncate(description, 1000),
                parsePrice(visibleText).orElse(null),
                zoneCity.city(),
                zoneCity.zone(),
                null,
                parseInteger(findFeature(featureSection, "Bedrooms").orElse(null)).orElse(0),
                parseInteger(findFeature(featureSection, "Bathrooms").orElse(null)).orElse(0),
                parseInteger(findFeature(featureSection, "Built Surface").orElse(null)).orElse(null),
                mapPropertyType(propertyType),
                mapOperationType(operation),
                true,
                normalizeDetailUrl(sourceUrl)
        );
    }

    String visibleText(String html) {
        String withoutScripts = html
                .replaceAll("(?is)<script[^>]*>.*?</script>", " ")
                .replaceAll("(?is)<style[^>]*>.*?</style>", " ")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</(p|div|li|tr|h1|h2|h3|span)>", "\n");
        return decodeHtml(TAG_PATTERN.matcher(withoutScripts).replaceAll(" "))
                .replace('\u00A0', ' ')
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("\\n\\s+", "\n")
                .replaceAll("\\n{2,}", "\n")
                .trim();
    }

    String cleanMojibake(String value) {
        if (value == null) {
            return "";
        }
        String cleaned = value
                .replace("â‚¬", "€")
                .replace("â¬", "€")
                .replace("Â€", "€")
                .replace("Âº", "º")
                .replace("Ã±", "ñ")
                .replace("Ã‘", "Ñ")
                .replace("Ã³", "ó")
                .replace("Ã©", "é")
                .replace("Ã¡", "á")
                .replace("Ãí", "í")
                .replace("Ãº", "ú")
                .replace("Ã¼", "ü")
                .replace('\u00A0', ' ');

        if (looksMojibake(cleaned)) {
            try {
                cleaned = new String(cleaned.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            } catch (RuntimeException ignored) {
                // Keep the targeted replacements above when a broad recode is not valid.
            }
        }
        return cleaned;
    }

    private boolean looksMojibake(String value) {
        return value.contains("Ã") || value.contains("Â") || value.contains("â");
    }

    String normalizeDetailUrl(String url) {
        URI uri = URI.create(url);
        String path = uri.getPath();
        int fichaIndex = path.toLowerCase(Locale.ROOT).indexOf("/ficha/");
        if (fichaIndex < 0 || uri.getScheme() == null || uri.getAuthority() == null) {
            return url;
        }

        String normalized = uri.getScheme() + "://" + uri.getAuthority() + path.substring(fichaIndex);
        if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
            normalized += "?" + uri.getQuery();
        }
        return URI.create(normalized).toString();
    }

    boolean isSupportedDetailUrl(String url) {
        URI uri = URI.create(url);
        String path = uri.getPath();
        if (path == null) {
            return false;
        }
        String normalizedPath = path.endsWith("/") ? path : path + "/";
        return normalizedPath.toLowerCase(Locale.ROOT).endsWith("/en/");
    }

    private String featureSection(String visibleText) {
        List<String> lines = lines(visibleText);
        int start = indexOfLine(lines, "Property Features");
        if (start < 0) {
            return "";
        }

        StringBuilder section = new StringBuilder();
        for (int i = start + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (isStopMarker(line)) {
                break;
            }
            section.append(line).append('\n');
        }
        return section.toString().trim();
    }

    private Optional<String> findFeature(String featureSection, String label) {
        List<String> lines = lines(featureSection);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.equalsIgnoreCase(label)) {
                for (int j = i + 1; j < lines.size(); j++) {
                    String next = lines.get(j);
                    if (!next.isBlank()) {
                        return Optional.of(next);
                    }
                }
                return Optional.empty();
            }

            String lowerLine = line.toLowerCase(Locale.ROOT);
            String lowerLabel = label.toLowerCase(Locale.ROOT);
            if (lowerLine.startsWith(lowerLabel + " ")) {
                String value = line.substring(label.length()).trim();
                if (!value.isBlank()) {
                    return Optional.of(value);
                }
            }
        }
        return Optional.empty();
    }

    private ZoneCity parseZoneCity(String value) {
        if (value == null || value.isBlank()) {
            return new ZoneCity(null, null);
        }
        String[] parts = value.split("/", 2);
        if (parts.length != 2) {
            return new ZoneCity(null, value.trim());
        }
        return new ZoneCity(parts[0].trim(), parts[1].trim());
    }

    private Optional<String> titleFrom(String visibleText, String propertyType, ZoneCity zoneCity) {
        for (String line : lines(textBefore(visibleText, "Property Features"))) {
            if (isPropertyHeading(line) && !isGenericHeading(line)) {
                return Optional.of(line);
            }
        }

        Optional<String> summary = descriptionFrom(visibleText);
        if (summary.isPresent()) {
            String beforeComma = summary.get().split(",", 2)[0].trim();
            if (isPropertyHeading(beforeComma) && !isGenericHeading(beforeComma)) {
                return Optional.of(beforeComma);
            }
        }

        if (propertyType != null && zoneCity.city() != null && zoneCity.zone() != null) {
            return Optional.of(propertyType.trim() + " - " + zoneCity.city() + " (" + zoneCity.zone() + ")");
        }
        return Optional.empty();
    }

    private boolean isPropertyHeading(String value) {
        return PROPERTY_HEADING_PATTERN.matcher(value.trim()).matches();
    }

    private boolean isGenericHeading(String value) {
        String normalized = normalize(value);
        return normalized.contains("encontramos la casa de tus sueños")
                || normalized.contains("encontramos la casa de tus sueã±os")
                || normalized.contains("encontramos la casa de tus sue");
    }

    private Optional<BigDecimal> parsePrice(String text) {
        String priceArea = textBetween(text, "Propose a price", "Before and After").orElse("");
        Matcher matcher = PRICE_PATTERN.matcher(priceArea);
        if (!matcher.find()) {
            return Optional.empty();
        }
        String normalized = matcher.group(1)
                .replace(".", "")
                .replaceAll(",\\d{2}$", "")
                .replace(",", "");
        try {
            return Optional.of(new BigDecimal(normalized));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private Optional<Integer> parseInteger(String value) {
        if (value == null) {
            return Optional.empty();
        }
        Matcher matcher = INTEGER_PATTERN.matcher(value);
        if (!matcher.find()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(matcher.group()));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private OperationType mapOperationType(String value) {
        String normalized = normalize(value);
        if (containsAny(normalized, "rent", "rental", "alquilar", "alquiler")) {
            return OperationType.RENT;
        }
        if (containsAny(normalized, "for sale", "sale", "comprar", "venta")) {
            return OperationType.SALE;
        }
        return OperationType.SALE;
    }

    private PropertyType mapPropertyType(String value) {
        String normalized = normalize(value);
        if (containsAny(normalized, "flat", "studio", "piso", "apartment", "apartamento", "estudio")) {
            return PropertyType.APARTMENT;
        }
        if (containsAny(normalized, "house", "casa", "chalet")) {
            return PropertyType.HOUSE;
        }
        if (containsAny(normalized, "garage", "garaje")) {
            return PropertyType.GARAGE;
        }
        return PropertyType.APARTMENT;
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private Optional<String> descriptionFrom(String visibleText) {
        for (String line : lines(textBefore(visibleText, "Property Features"))) {
            if (line.contains("Built Surface") && line.contains(",")) {
                return Optional.of(line);
            }
        }
        return Optional.empty();
    }

    private List<String> lines(String text) {
        return text.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }

    private int indexOfLine(List<String> lines, String value) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).equalsIgnoreCase(value)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isStopMarker(String line) {
        String normalizedLine = line.toLowerCase(Locale.ROOT);
        return FEATURE_STOP_MARKERS.stream()
                .anyMatch(marker -> normalizedLine.startsWith(marker.toLowerCase(Locale.ROOT)))
                || FOOTER_STOP_MARKERS.stream()
                .anyMatch(marker -> normalizedLine.contains(marker.toLowerCase(Locale.ROOT)));
    }

    private String textBefore(String text, String marker) {
        int index = text.toLowerCase(Locale.ROOT).indexOf(marker.toLowerCase(Locale.ROOT));
        if (index < 0) {
            return text;
        }
        return text.substring(0, index);
    }

    private Optional<String> textBetween(String text, String startMarker, String endMarker) {
        String lowerText = text.toLowerCase(Locale.ROOT);
        int start = lowerText.indexOf(startMarker.toLowerCase(Locale.ROOT));
        if (start < 0) {
            return Optional.empty();
        }
        int contentStart = start + startMarker.length();
        int end = lowerText.indexOf(endMarker.toLowerCase(Locale.ROOT), contentStart);
        if (end < 0) {
            return Optional.of(text.substring(contentStart));
        }
        return Optional.of(text.substring(contentStart, end));
    }

    private String decodeHtml(String value) {
        return value.replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&euro;", "€")
                .replace("&#8364;", "€");
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private record ZoneCity(String zone, String city) {
    }
}
