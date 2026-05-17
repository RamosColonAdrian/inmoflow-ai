package com.inmoflow.backend.conversation.application;

import com.inmoflow.backend.conversation.api.request.CreateConversationRequest;
import com.inmoflow.backend.conversation.api.request.CreateMessageRequest;
import com.inmoflow.backend.conversation.domain.Conversation;
import com.inmoflow.backend.conversation.domain.Message;
import com.inmoflow.backend.conversation.infrastructure.ConversationRepository;
import com.inmoflow.backend.conversation.infrastructure.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Transactional
    public Conversation create(CreateConversationRequest request) {
        Conversation conversation = Conversation.builder()
                .leadId(request.leadId())
                .channel(request.channel())
                .status(request.status())
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
    public Message addMessage(UUID conversationId, CreateMessageRequest request) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new NoSuchElementException("Conversation not found: " + conversationId);
        }

        Message message = Message.builder()
                .conversationId(conversationId)
                .senderType(request.senderType())
                .content(request.content())
                .aiGenerated(request.aiGenerated())
                .build();

        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<Message> findMessagesByConversationId(UUID conversationId) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new NoSuchElementException("Conversation not found: " + conversationId);
        }

        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);
    }
}
