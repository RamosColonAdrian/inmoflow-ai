package com.inmoflow.backend.appointment.infrastructure;

import com.inmoflow.backend.appointment.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByLeadId(UUID leadId);

    List<Appointment> findByConversationId(UUID conversationId);
}
