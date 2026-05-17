package com.inmoflow.backend.conversation.api.request;

import com.inmoflow.backend.conversation.domain.SenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMessageRequest(

        @NotNull(message = "Sender type is required")
        SenderType senderType,

        @NotBlank(message = "Message content is required")
        String content,

        @NotNull(message = "AI generated flag is required")
        Boolean aiGenerated
) {
}
