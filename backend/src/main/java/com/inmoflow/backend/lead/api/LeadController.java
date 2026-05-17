package com.inmoflow.backend.lead.api;

import com.inmoflow.backend.lead.api.request.CreateLeadRequest;
import com.inmoflow.backend.lead.api.response.LeadResponse;
import com.inmoflow.backend.lead.application.LeadService;
import com.inmoflow.backend.shared.api.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private static final int MAX_PAGE_SIZE = 100;

    private final LeadService leadService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeadResponse create(@Valid @RequestBody CreateLeadRequest request) {
        return LeadResponse.from(leadService.create(request));
    }

    @GetMapping
    public PageResponse<LeadResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return PageResponse.from(leadService.findAll(pageRequest(page, size))
                .map(LeadResponse::from));
    }

    private PageRequest pageRequest(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize);
    }
}
