package com.inmoflow.backend.conversation.api;

import com.inmoflow.backend.conversation.api.request.CreateConversationRequest;
import com.inmoflow.backend.conversation.api.request.CreateMessageRequest;
import com.inmoflow.backend.conversation.api.response.ConversationResponse;
import com.inmoflow.backend.conversation.api.response.MessageResponse;
import com.inmoflow.backend.conversation.application.ConversationService;
import com.inmoflow.backend.shared.api.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ConversationService conversationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse create(@Valid @RequestBody CreateConversationRequest request) {
        return ConversationResponse.from(conversationService.create(request));
    }

    @GetMapping
    public PageResponse<ConversationResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return PageResponse.from(conversationService.findAll(pageRequest(page, size))
                .map(ConversationResponse::from));
    }

    @GetMapping("/lead/{leadId}")
    public List<ConversationResponse> findByLeadId(@PathVariable UUID leadId) {
        return conversationService.findByLeadId(leadId)
                .stream()
                .map(ConversationResponse::from)
                .toList();
    }

    @PostMapping("/{conversationId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse addMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody CreateMessageRequest request
    ) {
        return MessageResponse.from(conversationService.addMessage(conversationId, request));
    }

    @GetMapping("/{conversationId}/messages")
    public List<MessageResponse> findMessagesByConversationId(@PathVariable UUID conversationId) {
        return conversationService.findMessagesByConversationId(conversationId)
                .stream()
                .map(MessageResponse::from)
                .toList();
    }

    private PageRequest pageRequest(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize);
    }
}
