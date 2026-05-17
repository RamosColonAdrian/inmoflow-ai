package com.inmoflow.backend.ai.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class OllamaClient {

    private final RestTemplate restTemplate;
    private final String chatUrl;

    public OllamaClient(
            @Value("${ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${ollama.timeout-ms:10000}") int timeoutMs
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);

        this.restTemplate = new RestTemplate(requestFactory);
        this.chatUrl = normalizeBaseUrl(baseUrl) + "/api/chat";
    }

    public OllamaChatResponse chat(OllamaChatRequest request) {
        try {
            return restTemplate.postForObject(chatUrl, request, OllamaChatResponse.class);
        } catch (RestClientException exception) {
            throw new IllegalStateException("Ollama request failed", exception);
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }

        return baseUrl;
    }
}
