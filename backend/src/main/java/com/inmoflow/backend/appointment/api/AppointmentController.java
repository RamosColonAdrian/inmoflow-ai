package com.inmoflow.backend.appointment.api;

import com.inmoflow.backend.appointment.api.request.CreateAppointmentRequest;
import com.inmoflow.backend.appointment.api.response.AppointmentResponse;
import com.inmoflow.backend.appointment.application.AppointmentService;
import com.inmoflow.backend.appointment.application.CreateAppointmentCommand;
import com.inmoflow.backend.shared.api.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private static final int MAX_PAGE_SIZE = 100;

    private final AppointmentService appointmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse create(@Valid @RequestBody CreateAppointmentRequest request) {
        CreateAppointmentCommand command = new CreateAppointmentCommand(
                request.agencyId(),
                request.leadId(),
                request.propertyId(),
                request.conversationId(),
                request.requestedDateText(),
                request.notes()
        );

        return AppointmentResponse.from(appointmentService.create(command));
    }

    @GetMapping
    public PageResponse<AppointmentResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return PageResponse.from(appointmentService.findAll(pageRequest(page, size))
                .map(AppointmentResponse::from));
    }

    @GetMapping("/lead/{leadId}")
    public List<AppointmentResponse> findByLeadId(@PathVariable UUID leadId) {
        return appointmentService.findByLeadId(leadId)
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    @GetMapping("/conversation/{conversationId}")
    public List<AppointmentResponse> findByConversationId(@PathVariable UUID conversationId) {
        return appointmentService.findByConversationId(conversationId)
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    private PageRequest pageRequest(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize);
    }
}
