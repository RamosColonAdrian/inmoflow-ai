package com.inmoflow.backend.ai.application;

import com.inmoflow.backend.conversation.domain.Message;
import com.inmoflow.backend.lead.domain.Lead;

import java.util.List;

public record AiResponseContext(
        String leadMessage,
        Lead lead,
        List<Message> recentMessages
) {
}
