package com.inmoflow.backend.conversation.application;

import com.inmoflow.backend.conversation.domain.ConversationChannel;
import com.inmoflow.backend.conversation.domain.ConversationStatus;

import java.util.UUID;

public record CreateConversationCommand(
        UUID leadId,
        ConversationChannel channel,
        ConversationStatus status
) {
}
