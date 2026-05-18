package com.inmoflow.backend.inbound.api;

import com.inmoflow.backend.inbound.application.InboundLeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inbound/leads")
@RequiredArgsConstructor
public class InboundLeadController {

    private final InboundLeadService inboundLeadService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InboundLeadResponse receive(@Valid @RequestBody InboundLeadRequest request) {
        return inboundLeadService.receive(request);
    }
}
