package com.inmoflow.backend.lead.api;

import com.inmoflow.backend.lead.api.request.CreateLeadRequest;
import com.inmoflow.backend.lead.api.response.LeadResponse;
import com.inmoflow.backend.lead.application.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeadResponse create(@Valid @RequestBody CreateLeadRequest request) {
        return LeadResponse.from(leadService.create(request));
    }

    @GetMapping
    public List<LeadResponse> findAll() {
        return leadService.findAll()
                .stream()
                .map(LeadResponse::from)
                .toList();
    }
}
