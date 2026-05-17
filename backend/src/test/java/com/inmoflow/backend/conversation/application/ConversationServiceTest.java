package com.inmoflow.backend.conversation.application;

import com.inmoflow.backend.ai.application.AiResponseGenerator;
import com.inmoflow.backend.ai.application.AiResponseContext;
import com.inmoflow.backend.ai.application.ControlledVisitResponseGenerator;
import com.inmoflow.backend.appointment.domain.Appointment;
import com.inmoflow.backend.appointment.application.AppointmentService;
import com.inmoflow.backend.appointment.application.CreateAppointmentCommand;
import com.inmoflow.backend.conversation.domain.Conversation;
import com.inmoflow.backend.conversation.domain.ConversationChannel;
import com.inmoflow.backend.conversation.domain.ConversationStatus;
import com.inmoflow.backend.conversation.domain.Message;
import com.inmoflow.backend.conversation.domain.SenderType;
import com.inmoflow.backend.conversation.infrastructure.ConversationRepository;
import com.inmoflow.backend.conversation.infrastructure.MessageRepository;
import com.inmoflow.backend.lead.application.LeadService;
import com.inmoflow.backend.lead.domain.Lead;
import com.inmoflow.backend.lead.infrastructure.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private LeadService leadService;

    @Mock
    private AiResponseGenerator aiResponseGenerator;

    @Mock
    private AppointmentService appointmentService;

    private ConversationService conversationService;

    @BeforeEach
    void setUp() {
        conversationService = new ConversationService(
                conversationRepository,
                messageRepository,
                leadRepository,
                leadService,
                aiResponseGenerator,
                new ControlledVisitResponseGenerator(),
                appointmentService
        );
    }

    @Test
    void createsRequestedAppointmentWhenLeadRequestsVisitWithAvailability() {
        UUID conversationId = UUID.randomUUID();
        UUID leadId = UUID.randomUUID();
        UUID agencyId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        Conversation conversation = conversation(conversationId, leadId);
        Lead lead = Lead.builder()
                .id(leadId)
                .agencyId(agencyId)
                .propertyId(propertyId)
                .phone("600123123")
                .build();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(leadRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(messageRepository.findTop10ByConversationIdOrderBySentAtDesc(conversationId)).thenReturn(List.of());
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> savedMessage(invocation.getArgument(0)));
        when(appointmentService.createRequestedIfAbsent(any(CreateAppointmentCommand.class)))
                .thenReturn(Optional.of(Appointment.builder().id(UUID.randomUUID()).build()));

        conversationService.addMessage(conversationId, new CreateMessageCommand(
                SenderType.LEAD,
                "Hola, quiero ver el piso el jueves por la tarde",
                false
        ));

        ArgumentCaptor<CreateAppointmentCommand> commandCaptor = ArgumentCaptor.forClass(CreateAppointmentCommand.class);
        verify(appointmentService).createRequestedIfAbsent(commandCaptor.capture());
        CreateAppointmentCommand command = commandCaptor.getValue();
        assertThat(command.agencyId()).isEqualTo(agencyId);
        assertThat(command.leadId()).isEqualTo(leadId);
        assertThat(command.propertyId()).isEqualTo(propertyId);
        assertThat(command.conversationId()).isEqualTo(conversationId);
        assertThat(command.requestedDateText()).isEqualTo("jueves por la tarde");
        assertThat(command.notes()).isEqualTo("Appointment requested automatically from lead message");
        verify(leadService).qualifyForVisitRequest(leadId);
    }

    @Test
    void doesNotCreateAppointmentWhenVisitRequestHasNoAvailability() {
        UUID conversationId = UUID.randomUUID();
        UUID leadId = UUID.randomUUID();
        Conversation conversation = conversation(conversationId, leadId);
        Lead lead = Lead.builder()
                .id(leadId)
                .agencyId(UUID.randomUUID())
                .build();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(leadRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(messageRepository.findTop10ByConversationIdOrderBySentAtDesc(conversationId)).thenReturn(List.of());
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> savedMessage(invocation.getArgument(0)));

        conversationService.addMessage(conversationId, new CreateMessageCommand(
                SenderType.LEAD,
                "Hola, quiero visitar el piso",
                false
        ));

        verify(appointmentService, never()).createRequestedIfAbsent(any(CreateAppointmentCommand.class));
        verify(leadService).markContactedForVisitInterest(leadId);
    }

    @Test
    void doesNotUpdateLeadWhenMessageHasNoVisitIntent() {
        UUID conversationId = UUID.randomUUID();
        UUID leadId = UUID.randomUUID();
        Conversation conversation = conversation(conversationId, leadId);
        Lead lead = Lead.builder()
                .id(leadId)
                .agencyId(UUID.randomUUID())
                .build();
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(leadRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(messageRepository.findTop10ByConversationIdOrderBySentAtDesc(conversationId)).thenReturn(List.of());
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> savedMessage(invocation.getArgument(0)));
        when(aiResponseGenerator.generateResponse(any(AiResponseContext.class))).thenReturn("Respuesta generica");

        conversationService.addMessage(conversationId, new CreateMessageCommand(
                SenderType.LEAD,
                "Hola, me interesa saber mas del piso",
                false
        ));

        verify(appointmentService, never()).createRequestedIfAbsent(any(CreateAppointmentCommand.class));
        verify(leadService, never()).markContactedForVisitInterest(any(UUID.class));
        verify(leadService, never()).qualifyForVisitRequest(any(UUID.class));
    }

    private Conversation conversation(UUID conversationId, UUID leadId) {
        return Conversation.builder()
                .id(conversationId)
                .leadId(leadId)
                .channel(ConversationChannel.WHATSAPP)
                .status(ConversationStatus.OPEN)
                .build();
    }

    private Message savedMessage(Message message) {
        if (message.getId() == null) {
            message.setId(UUID.randomUUID());
        }
        if (message.getSentAt() == null) {
            message.setSentAt(LocalDateTime.now());
        }
        return message;
    }
}
