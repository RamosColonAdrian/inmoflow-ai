package com.inmoflow.backend.conversation.application;

import com.inmoflow.backend.conversation.domain.SenderType;

public record CreateMessageCommand(
        SenderType senderType,
        String content,
        Boolean aiGenerated
) {
}
