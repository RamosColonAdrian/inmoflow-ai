package com.inmoflow.backend.ai.api;

import com.inmoflow.backend.ai.application.AiAssistantMockService;
import com.inmoflow.backend.conversation.api.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @PostMapping("/conversations/{conversationId}/send-response")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse sendResponse(@PathVariable UUID conversationId) {
        return MessageResponse.from(aiAssistantMockService.sendResponse(conversationId));
    }
}
