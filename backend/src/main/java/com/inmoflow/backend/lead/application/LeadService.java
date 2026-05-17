package com.inmoflow.backend.lead.application;

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
    public Lead create(CreateLeadCommand command) {
        Lead lead = Lead.builder()
                .agencyId(command.agencyId())
                .propertyId(command.propertyId())
                .name(command.name())
                .email(command.email())
                .phone(command.phone())
                .source(command.source())
                .status(command.status())
                .message(command.message())
                .budget(command.budget())
                .desiredZone(command.desiredZone())
                .score(command.score())
                .build();

        return leadRepository.save(lead);
    }

    @Transactional(readOnly = true)
    public Page<Lead> findAll(Pageable pageable) {
        return leadRepository.findAll(pageable);
    }
}
