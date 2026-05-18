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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InboundLeadServiceTest {

    @Mock
    private LeadService leadService;

    @Mock
    private ConversationService conversationService;

    private InboundLeadService inboundLeadService;

    @BeforeEach
    void setUp() {
        inboundLeadService = new InboundLeadService(leadService, conversationService);
    }

    @Test
    void createsLeadConversationAndInitialLeadMessage() {
        UUID agencyId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        UUID leadId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        String message = "Hola, quiero visitar el piso el jueves por la tarde";
        InboundLeadRequest request = new InboundLeadRequest(
                agencyId,
                propertyId,
                "WEB_FORM",
                "Carlos Perez",
                "carlos@test.com",
                "611111111",
                message
        );
        Lead lead = Lead.builder()
                .id(leadId)
                .agencyId(agencyId)
                .propertyId(propertyId)
                .source(LeadSource.WEB_FORM)
                .status(LeadStatus.NEW)
                .score(0)
                .build();
        Conversation conversation = Conversation.builder()
                .id(conversationId)
                .leadId(leadId)
                .channel(ConversationChannel.WEB_CHAT)
                .status(ConversationStatus.OPEN)
                .build();
        when(leadService.create(org.mockito.ArgumentMatchers.any(CreateLeadCommand.class))).thenReturn(lead);
        when(conversationService.create(org.mockito.ArgumentMatchers.any(CreateConversationCommand.class)))
                .thenReturn(conversation);

        InboundLeadResponse response = inboundLeadService.receive(request);

        ArgumentCaptor<CreateLeadCommand> leadCommandCaptor = ArgumentCaptor.forClass(CreateLeadCommand.class);
        verify(leadService).create(leadCommandCaptor.capture());
        CreateLeadCommand leadCommand = leadCommandCaptor.getValue();
        assertThat(leadCommand.agencyId()).isEqualTo(agencyId);
        assertThat(leadCommand.propertyId()).isEqualTo(propertyId);
        assertThat(leadCommand.name()).isEqualTo("Carlos Perez");
        assertThat(leadCommand.email()).isEqualTo("carlos@test.com");
        assertThat(leadCommand.phone()).isEqualTo("611111111");
        assertThat(leadCommand.source()).isEqualTo(LeadSource.WEB_FORM);
        assertThat(leadCommand.status()).isEqualTo(LeadStatus.NEW);
        assertThat(leadCommand.message()).isEqualTo(message);
        assertThat(leadCommand.score()).isZero();

        ArgumentCaptor<CreateConversationCommand> conversationCommandCaptor =
                ArgumentCaptor.forClass(CreateConversationCommand.class);
        verify(conversationService).create(conversationCommandCaptor.capture());
        assertThat(conversationCommandCaptor.getValue().leadId()).isEqualTo(leadId);
        assertThat(conversationCommandCaptor.getValue().channel()).isEqualTo(ConversationChannel.WEB_CHAT);
        assertThat(conversationCommandCaptor.getValue().status()).isEqualTo(ConversationStatus.OPEN);

        ArgumentCaptor<CreateMessageCommand> messageCommandCaptor = ArgumentCaptor.forClass(CreateMessageCommand.class);
        verify(conversationService).addMessage(org.mockito.ArgumentMatchers.eq(conversationId), messageCommandCaptor.capture());
        assertThat(messageCommandCaptor.getValue().senderType()).isEqualTo(SenderType.LEAD);
        assertThat(messageCommandCaptor.getValue().content()).isEqualTo(message);
        assertThat(messageCommandCaptor.getValue().aiGenerated()).isFalse();

        assertThat(response.leadId()).isEqualTo(leadId);
        assertThat(response.conversationId()).isEqualTo(conversationId);
        assertThat(response.leadStatus()).isEqualTo(LeadStatus.NEW);
        assertThat(response.leadScore()).isZero();
        assertThat(response.source()).isEqualTo(LeadSource.WEB_FORM);
        assertThat(response.messageReceived()).isEqualTo(message);
        assertThat(response.appointmentCreated()).isFalse();
        assertThat(response.botResponseCreated()).isTrue();
    }

    @Test
    void rejectsInvalidLeadSource() {
        InboundLeadRequest request = new InboundLeadRequest(
                UUID.randomUUID(),
                null,
                "PORTAL_X",
                "Carlos Perez",
                null,
                null,
                "Hola"
        );

        assertThatThrownBy(() -> inboundLeadService.receive(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid lead source: PORTAL_X");

        verifyNoInteractions(leadService, conversationService);
    }
}
