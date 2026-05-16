package com.inmoflow.backend.lead.application;

import com.inmoflow.backend.lead.api.request.CreateLeadRequest;
import com.inmoflow.backend.lead.domain.Lead;
import com.inmoflow.backend.lead.infrastructure.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public List<Lead> findAll() {
        return leadRepository.findAll();
    }
}
