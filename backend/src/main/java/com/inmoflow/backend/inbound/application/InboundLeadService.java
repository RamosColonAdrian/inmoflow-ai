package com.inmoflow.backend.inbound.application;

import com.inmoflow.backend.conversation.application.ConversationService;
import com.inmoflow.backend.conversation.application.CreateConversationCommand;
import com.inmoflow.backend.conversation.application.CreateMessageCommand;
import com.inmoflow.backend.conversation.domain.Conversation;
import com.inmoflow.backend.conversation.domain.ConversationChannel;
import com.inmoflow.backend.conversation.domain.ConversationStatus;
import com.inmoflow.backend.conversation.domain.SenderType;
import com.inmoflow.backend.inbound.api.InboundLeadRequest;
import com.inmoflow.backend.inbound.api.InboundLeadResponse;
import com.inmoflow.backend.lead.application.CreateLeadCommand;
import com.inmoflow.backend.lead.application.LeadService;
import com.inmoflow.backend.lead.domain.Lead;
import com.inmoflow.backend.lead.domain.LeadSource;
import com.inmoflow.backend.lead.domain.LeadStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class InboundLeadService {

    private final LeadService leadService;
    private final ConversationService conversationService;

    @Transactional
    public InboundLeadResponse receive(InboundLeadRequest request) {
        LeadSource source = parseSource(request.source());

        Lead lead = leadService.create(new CreateLeadCommand(
                request.agencyId(),
                request.propertyId(),
                request.name(),
                request.email(),
                request.phone(),
                source,
                LeadStatus.NEW,
                request.message(),
                null,
                null,
                0
        ));

        Conversation conversation = conversationService.create(new CreateConversationCommand(
                lead.getId(),
                channelFor(source),
                ConversationStatus.OPEN
        ));

        conversationService.addMessage(conversation.getId(), new CreateMessageCommand(
                SenderType.LEAD,
                request.message(),
                false
        ));

        return new InboundLeadResponse(
                lead.getId(),
                conversation.getId(),
                lead.getStatus(),
                lead.getScore(),
                source,
                request.message(),
                false,
                true
        );
    }

    private LeadSource parseSource(String source) {
        try {
            return LeadSource.valueOf(source.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid lead source: " + source);
        }
    }

    private ConversationChannel channelFor(LeadSource source) {
        return switch (source) {
            case EMAIL, IDEALISTA_EMAIL, FOTOCASA_EMAIL -> ConversationChannel.EMAIL;
            case WHATSAPP -> ConversationChannel.WHATSAPP;
            case WEB_FORM -> ConversationChannel.WEB_CHAT;
            case MANUAL -> ConversationChannel.MANUAL;
        };
    }
}
