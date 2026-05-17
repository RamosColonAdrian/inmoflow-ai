package com.inmoflow.backend.conversation.api.response;

import com.inmoflow.backend.conversation.domain.Conversation;
import com.inmoflow.backend.conversation.domain.ConversationChannel;
import com.inmoflow.backend.conversation.domain.ConversationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        UUID leadId,
        ConversationChannel channel,
        ConversationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ConversationResponse from(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getLeadId(),
                conversation.getChannel(),
                conversation.getStatus(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }
}
