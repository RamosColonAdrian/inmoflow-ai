package com.inmoflow.backend.conversation.application;

import com.inmoflow.backend.ai.application.AiResponseContext;
import com.inmoflow.backend.ai.application.AiResponseGenerator;
import com.inmoflow.backend.conversation.domain.Conversation;
import com.inmoflow.backend.conversation.domain.Message;
import com.inmoflow.backend.conversation.domain.SenderType;
import com.inmoflow.backend.conversation.infrastructure.ConversationRepository;
import com.inmoflow.backend.conversation.infrastructure.MessageRepository;
import com.inmoflow.backend.lead.domain.Lead;
import com.inmoflow.backend.lead.infrastructure.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final LeadRepository leadRepository;
    private final AiResponseGenerator aiResponseGenerator;

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
            CreateMessageCommand aiResponseCommand = new CreateMessageCommand(
                    SenderType.BOT,
                    aiResponseGenerator.generateResponse(context),
                    true
            );
            saveMessage(conversationId, aiResponseCommand);
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

    @Transactional(readOnly = true)
    public List<Message> findMessagesByConversationId(UUID conversationId) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new NoSuchElementException("Conversation not found: " + conversationId);
        }

        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);
    }
}
