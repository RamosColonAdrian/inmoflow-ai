package com.inmoflow.backend.appointment.application;

import com.inmoflow.backend.appointment.domain.Appointment;
import com.inmoflow.backend.appointment.domain.AppointmentStatus;
import com.inmoflow.backend.appointment.infrastructure.AppointmentRepository;
import com.inmoflow.backend.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void createsRequestedAppointmentWhenConversationHasNoRequestedAppointment() {
        UUID conversationId = UUID.randomUUID();
        CreateAppointmentCommand command = command(conversationId);
        when(appointmentRepository.existsByConversationIdAndStatus(conversationId, AppointmentStatus.REQUESTED))
                .thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Appointment> created = appointmentService.createRequestedIfAbsent(command);

        assertThat(created).isPresent();
        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(appointmentCaptor.capture());
        assertThat(appointmentCaptor.getValue().getStatus()).isEqualTo(AppointmentStatus.REQUESTED);
        assertThat(appointmentCaptor.getValue().getConversationId()).isEqualTo(conversationId);
        assertThat(appointmentCaptor.getValue().getRequestedDateText()).isEqualTo("jueves por la tarde");
    }

    @Test
    void doesNotCreateAppointmentWhenConversationAlreadyHasRequestedAppointment() {
        UUID conversationId = UUID.randomUUID();
        when(appointmentRepository.existsByConversationIdAndStatus(conversationId, AppointmentStatus.REQUESTED))
                .thenReturn(true);

        Optional<Appointment> created = appointmentService.createRequestedIfAbsent(command(conversationId));

        assertThat(created).isEmpty();
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void updatesAppointmentStatusAndUpdatedAt() {
        UUID appointmentId = UUID.randomUUID();
        LocalDateTime previousUpdatedAt = LocalDateTime.now().minusDays(1);
        Appointment appointment = Appointment.builder()
                .id(appointmentId)
                .agencyId(UUID.randomUUID())
                .leadId(UUID.randomUUID())
                .status(AppointmentStatus.REQUESTED)
                .createdAt(previousUpdatedAt)
                .updatedAt(previousUpdatedAt)
                .build();
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment updated = appointmentService.updateStatus(appointmentId, "CONFIRMED");

        assertThat(updated.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(updated.getUpdatedAt()).isAfter(previousUpdatedAt);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void throwsResourceNotFoundWhenUpdatingMissingAppointment() {
        UUID appointmentId = UUID.randomUUID();
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.updateStatus(appointmentId, "CONFIRMED"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Appointment not found: " + appointmentId);

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void throwsBadRequestWhenUpdatingWithInvalidStatus() {
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = Appointment.builder()
                .id(appointmentId)
                .agencyId(UUID.randomUUID())
                .leadId(UUID.randomUUID())
                .status(AppointmentStatus.REQUESTED)
                .build();
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.updateStatus(appointmentId, "APPROVED"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid appointment status: APPROVED");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    private CreateAppointmentCommand command(UUID conversationId) {
        return new CreateAppointmentCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                conversationId,
                "jueves por la tarde",
                "Appointment requested automatically from lead message"
        );
    }
}
