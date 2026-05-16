package com.inmoflow.backend.agency.api;

import com.inmoflow.backend.agency.api.request.CreateAgencyRequest;
import com.inmoflow.backend.agency.api.response.AgencyResponse;
import com.inmoflow.backend.agency.application.AgencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agencies")
@RequiredArgsConstructor
public class AgencyController {

    private final AgencyService agencyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgencyResponse create(@Valid @RequestBody CreateAgencyRequest request) {
        return AgencyResponse.from(agencyService.create(request));
    }

    @GetMapping
    public List<AgencyResponse> findAll() {
        return agencyService.findAll()
                .stream()
                .map(AgencyResponse::from)
                .toList();
    }
}