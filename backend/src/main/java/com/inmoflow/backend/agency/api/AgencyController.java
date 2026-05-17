package com.inmoflow.backend.agency.api;

import com.inmoflow.backend.agency.api.request.CreateAgencyRequest;
import com.inmoflow.backend.agency.api.response.AgencyResponse;
import com.inmoflow.backend.agency.application.AgencyService;
import com.inmoflow.backend.shared.api.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agencies")
@RequiredArgsConstructor
public class AgencyController {

    private static final int MAX_PAGE_SIZE = 100;

    private final AgencyService agencyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgencyResponse create(@Valid @RequestBody CreateAgencyRequest request) {
        return AgencyResponse.from(agencyService.create(request));
    }

    @GetMapping
    public PageResponse<AgencyResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return PageResponse.from(agencyService.findAll(pageRequest(page, size))
                .map(AgencyResponse::from));
    }

    private PageRequest pageRequest(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize);
    }
}
