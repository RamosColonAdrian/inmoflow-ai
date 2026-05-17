package com.inmoflow.backend.appointment.application;

import com.inmoflow.backend.appointment.domain.Appointment;
import com.inmoflow.backend.appointment.domain.AppointmentStatus;
import com.inmoflow.backend.appointment.infrastructure.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    @Transactional
    public Appointment create(CreateAppointmentCommand command) {
        Appointment appointment = Appointment.builder()
                .agencyId(command.agencyId())
                .leadId(command.leadId())
                .propertyId(command.propertyId())
                .conversationId(command.conversationId())
                .requestedDateText(command.requestedDateText())
                .status(AppointmentStatus.REQUESTED)
                .notes(command.notes())
                .build();

        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public Page<Appointment> findAll(Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findByLeadId(UUID leadId) {
        return appointmentRepository.findByLeadId(leadId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findByConversationId(UUID conversationId) {
        return appointmentRepository.findByConversationId(conversationId);
    }
}
