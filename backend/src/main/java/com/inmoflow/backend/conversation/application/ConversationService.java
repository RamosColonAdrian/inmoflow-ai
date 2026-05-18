package com.inmoflow.backend.conversation.application;

import com.inmoflow.backend.ai.application.AiResponseContext;
import com.inmoflow.backend.ai.application.AiResponseGenerator;
import com.inmoflow.backend.ai.application.ControlledVisitResponse;
import com.inmoflow.backend.ai.application.ControlledVisitResponseGenerator;
import com.inmoflow.backend.appointment.application.AppointmentService;
import com.inmoflow.backend.appointment.application.CreateAppointmentCommand;
import com.inmoflow.backend.conversation.domain.Conversation;
import com.inmoflow.backend.conversation.domain.Message;
import com.inmoflow.backend.conversation.domain.SenderType;
import com.inmoflow.backend.conversation.infrastructure.ConversationRepository;
import com.inmoflow.backend.conversation.infrastructure.MessageRepository;
import com.inmoflow.backend.lead.application.LeadService;
import com.inmoflow.backend.lead.application.LeadQualificationService;
import com.inmoflow.backend.lead.application.PropertyRuleEvaluation;
import com.inmoflow.backend.lead.application.PropertyRuleEvaluationResult;
import com.inmoflow.backend.lead.domain.Lead;
import com.inmoflow.backend.lead.infrastructure.LeadRepository;
import com.inmoflow.backend.property.domain.Property;
import com.inmoflow.backend.property.infrastructure.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private static final String AUTOMATIC_APPOINTMENT_NOTES = "Appointment requested automatically from lead message";

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final LeadRepository leadRepository;
    private final PropertyRepository propertyRepository;
    private final LeadService leadService;
    private final LeadQualificationService leadQualificationService;
    private final AiResponseGenerator aiResponseGenerator;
    private final ControlledVisitResponseGenerator controlledVisitResponseGenerator;
    private final AppointmentService appointmentService;

    @Transactional
    public Conversation create(CreateConversationCommand command) {
        Conversation conversation = Conversation.builder()
                .leadId(command.leadId())
                .channel(command.channel())
                .status(command.status())
                .build();

        return conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public Page<Conversation> findAll(Pageable pageable) {
        return conversationRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Conversation> findByLeadId(UUID leadId) {
        return conversationRepository.findByLeadId(leadId);
    }

    @Transactional
    public Message addMessage(UUID conversationId, CreateMessageCommand command) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));

        Message message = saveMessage(conversationId, command);

        if (command.senderType() == SenderType.LEAD) {
            AiResponseContext context = buildAiResponseContext(conversation, message);
            Optional<ControlledVisitResponse> controlledVisitResponse = controlledVisitResponseGenerator.generate(context);
            String botResponse = controlledVisitResponse
                    .map(response -> handleControlledVisitResponse(conversation, context.lead(), response, context.leadMessage()))
                    .orElseGet(() -> aiResponseGenerator.generateResponse(context));
            saveMessage(conversationId, new CreateMessageCommand(SenderType.BOT, botResponse, true));
        }

        return message;
    }

    private Message saveMessage(UUID conversationId, CreateMessageCommand command) {
        Message message = Message.builder()
                .conversationId(conversationId)
                .senderType(command.senderType())
                .content(command.content())
                .aiGenerated(command.aiGenerated())
                .build();

        return messageRepository.save(message);
    }

    private AiResponseContext buildAiResponseContext(Conversation conversation, Message leadMessage) {
        Lead lead = leadRepository.findById(conversation.getLeadId()).orElse(null);
        List<Message> recentMessages = new ArrayList<>(
                messageRepository.findTop10ByConversationIdOrderBySentAtDesc(conversation.getId())
        );
        recentMessages.sort(Comparator.comparing(Message::getSentAt));

        return new AiResponseContext(leadMessage.getContent(), lead, recentMessages);
    }

    private String handleControlledVisitResponse(Conversation conversation, Lead lead, ControlledVisitResponse response, String leadMessage) {
        if (lead == null) {
            return response.response();
        }

        if (!response.hasRequestedDateText()) {
            leadService.markContactedForVisitInterest(lead.getId());
            return response.response();
        }

        Optional<Property> property = loadRelatedProperty(lead);
        String qualificationRulesText = property
                .map(Property::getQualificationRulesText)
                .orElse(null);

        if (hasText(qualificationRulesText)) {
            PropertyRuleEvaluation evaluation = leadQualificationService.evaluate(qualificationRulesText, leadMessage);
            if (evaluation.result() == PropertyRuleEvaluationResult.NEEDS_MORE_INFO) {
                leadService.markContactedForVisitInterest(lead.getId());
                return qualificationInfoRequestResponse(response.requestedDateText(), evaluation);
            }
            if (evaluation.result() == PropertyRuleEvaluationResult.MISMATCH) {
                leadService.markNeedsHumanForQualificationMismatch(lead.getId());
                return qualificationMismatchResponse(evaluation);
            }
        }

        CreateAppointmentCommand command = new CreateAppointmentCommand(
                lead.getAgencyId(),
                conversation.getLeadId(),
                lead.getPropertyId(),
                conversation.getId(),
                response.requestedDateText(),
                AUTOMATIC_APPOINTMENT_NOTES
        );

        appointmentService.createRequestedIfAbsent(command)
                .ifPresent(appointment -> leadService.qualifyForVisitRequest(lead.getId()));
        return response.response();
    }

    private Optional<Property> loadRelatedProperty(Lead lead) {
        if (lead.getPropertyId() == null) {
            return Optional.empty();
        }
        return propertyRepository.findById(lead.getPropertyId());
    }

    private String qualificationInfoRequestResponse(String requestedDateText, PropertyRuleEvaluation evaluation) {
        return "Perfecto, he anotado que te vendria bien " + requestedDateText
                + ". Antes de pasar la solicitud al agente, para este inmueble se solicitan algunas condiciones. "
                + "Podrias confirmarme " + joinQuestions(evaluation.missingQuestions()) + "?";
    }

    private String qualificationMismatchResponse(PropertyRuleEvaluation evaluation) {
        return "Gracias por tu interes. Te comento que este inmueble tiene algunas condiciones del propietario y, en principio, "
                + evaluation.mismatchMessage()
                + ". Si quieres, un agente puede revisar tu caso igualmente.";
    }

    private String joinQuestions(List<String> questions) {
        if (questions.isEmpty()) {
            return "si cumples las condiciones del inmueble";
        }
        if (questions.size() == 1) {
            return questions.getFirst();
        }
        return String.join(", ", questions.subList(0, questions.size() - 1))
                + " y "
                + questions.getLast();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @Transactional(readOnly = true)
    public List<Message> findMessagesByConversationId(UUID conversationId) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new NoSuchElementException("Conversation not found: " + conversationId);
        }

        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);
    }
}
