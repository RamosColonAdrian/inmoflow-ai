package com.inmoflow.backend.lead.application;

import com.inmoflow.backend.lead.api.request.CreateLeadRequest;
import com.inmoflow.backend.lead.domain.Lead;
import com.inmoflow.backend.lead.infrastructure.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;

    @Transactional
    public Lead create(CreateLeadRequest request) {
        Lead lead = Lead.builder()
                .agencyId(request.agencyId())
                .propertyId(request.propertyId())
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .source(request.source())
                .status(request.status())
                .message(request.message())
                .budget(request.budget())
                .desiredZone(request.desiredZone())
                .score(request.score())
                .build();

        return leadRepository.save(lead);
    }

    @Transactional(readOnly = true)
    public Page<Lead> findAll(Pageable pageable) {
        return leadRepository.findAll(pageable);
    }
}
