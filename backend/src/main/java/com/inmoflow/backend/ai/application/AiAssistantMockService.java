package com.inmoflow.backend.ai.application;

import com.inmoflow.backend.ai.api.AiSuggestedResponse;
import com.inmoflow.backend.ai.domain.AiIntent;
import com.inmoflow.backend.conversation.application.CreateMessageCommand;
import com.inmoflow.backend.conversation.application.ConversationService;
import com.inmoflow.backend.conversation.domain.Message;
import com.inmoflow.backend.conversation.domain.SenderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiAssistantMockService {

    private static final String MOCK_RESPONSE = "Hola, gracias por tu interes. El inmueble sigue disponible. \u00bfTe gustaria agendar una visita esta semana?";
    private static final double MOCK_CONFIDENCE = 0.85;

    private final ConversationService conversationService;

    @Transactional(readOnly = true)
    public AiSuggestedResponse suggestResponse(UUID conversationId) {
        conversationService.findMessagesByConversationId(conversationId);

        return new AiSuggestedResponse(
                conversationId,
                MOCK_RESPONSE,
                AiIntent.ASK_VISIT.name(),
                MOCK_CONFIDENCE,
                false
        );
    }

    @Transactional
    public Message sendResponse(UUID conversationId) {
        CreateMessageCommand command = new CreateMessageCommand(
                SenderType.BOT,
                MOCK_RESPONSE,
                true
        );

        return conversationService.addMessage(conversationId, command);
    }
}
