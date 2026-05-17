package com.inmoflow.backend.ai.api;

import com.inmoflow.backend.ai.application.AiAssistantMockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAssistantController {

    private final AiAssistantMockService aiAssistantMockService;

    @PostMapping("/conversations/{conversationId}/suggest-response")
    public AiSuggestedResponse suggestResponse(@PathVariable UUID conversationId) {
        return aiAssistantMockService.suggestResponse(conversationId);
    }
}
