package com.inmoflow.backend.propertyimport.infrastructure;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HttpPageFetcher {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final Pattern META_CHARSET_PATTERN = Pattern.compile("(?is)<meta[^>]+charset\\s*=\\s*[\"']?([^\\s\"'>;]+)");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public String fetch(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(TIMEOUT)
                    .header("User-Agent", "InmoFlowBot/0.1 (+https://inmoflow.local)")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalArgumentException("Could not fetch page " + url + ". HTTP status: " + response.statusCode());
            }
            return decodeBody(response);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not fetch page " + url + ": " + exception.getMessage(), exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException("Interrupted while fetching page " + url, exception);
        }
    }

    private String decodeBody(HttpResponse<byte[]> response) {
        byte[] body = response.body();
        Optional<Charset> contentTypeCharset = response.headers()
                .firstValue("Content-Type")
                .flatMap(this::charsetFromContentType);
        if (contentTypeCharset.isPresent()) {
            return new String(body, contentTypeCharset.get());
        }

        String utf8 = new String(body, StandardCharsets.UTF_8);
        Optional<Charset> metaCharset = charsetFromMeta(utf8);
        return metaCharset
                .map(charset -> new String(body, charset))
                .orElse(utf8);
    }

    private Optional<Charset> charsetFromContentType(String contentType) {
        for (String part : contentType.split(";")) {
            String trimmed = part.trim();
            if (trimmed.toLowerCase().startsWith("charset=")) {
                return charsetByName(trimmed.substring("charset=".length()).trim());
            }
        }
        return Optional.empty();
    }

    private Optional<Charset> charsetFromMeta(String html) {
        Matcher matcher = META_CHARSET_PATTERN.matcher(html);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return charsetByName(matcher.group(1).trim());
    }

    private Optional<Charset> charsetByName(String charsetName) {
        try {
            return Optional.of(Charset.forName(charsetName.replace("\"", "").replace("'", "")));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }
}
