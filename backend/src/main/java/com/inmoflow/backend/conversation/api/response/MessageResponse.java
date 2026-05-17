package com.inmoflow.backend.conversation.api.response;

import com.inmoflow.backend.conversation.domain.Message;
import com.inmoflow.backend.conversation.domain.SenderType;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID conversationId,
        SenderType senderType,
        String content,
        Boolean aiGenerated,
        LocalDateTime sentAt
) {

    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversationId(),
                message.getSenderType(),
                message.getContent(),
                message.getAiGenerated(),
                message.getSentAt()
        );
    }
}
