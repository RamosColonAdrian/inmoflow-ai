package com.inmoflow.backend.conversation.api.request;

import com.inmoflow.backend.conversation.domain.ConversationChannel;
import com.inmoflow.backend.conversation.domain.ConversationStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateConversationRequest(

        @NotNull(message = "Lead id is required")
        UUID leadId,

        @NotNull(message = "Conversation channel is required")
        ConversationChannel channel,

        @NotNull(message = "Conversation status is required")
        ConversationStatus status
) {
}
