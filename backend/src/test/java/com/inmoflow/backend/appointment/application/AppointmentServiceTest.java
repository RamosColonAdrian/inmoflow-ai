package com.inmoflow.backend.appointment.application;

import com.inmoflow.backend.appointment.domain.Appointment;
import com.inmoflow.backend.appointment.domain.AppointmentStatus;
import com.inmoflow.backend.appointment.infrastructure.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
