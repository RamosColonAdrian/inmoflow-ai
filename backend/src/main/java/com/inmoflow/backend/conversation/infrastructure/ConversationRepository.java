package com.inmoflow.backend.conversation.infrastructure;

import com.inmoflow.backend.conversation.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    List<Conversation> findByLeadId(UUID leadId);
}
