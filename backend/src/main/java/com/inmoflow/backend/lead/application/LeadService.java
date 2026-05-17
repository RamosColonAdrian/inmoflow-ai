package com.inmoflow.backend.lead.application;

import com.inmoflow.backend.lead.domain.Lead;
import com.inmoflow.backend.lead.domain.LeadStatus;
import com.inmoflow.backend.lead.infrastructure.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadService {

    private static final int VISIT_INTEREST_SCORE = 70;
    private static final int QUALIFIED_VISIT_SCORE = 90;

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

    @Transactional
    public Lead qualifyForVisitRequest(UUID leadId) {
        Lead lead = findByIdOrThrow(leadId);
        lead.setStatus(LeadStatus.QUALIFIED);
        lead.setScore(maxScore(lead.getScore(), QUALIFIED_VISIT_SCORE));
        return leadRepository.save(lead);
    }

    @Transactional
    public Lead markContactedForVisitInterest(UUID leadId) {
        Lead lead = findByIdOrThrow(leadId);
        if (lead.getStatus() == LeadStatus.NEW) {
            lead.setStatus(LeadStatus.CONTACTED);
        }
        lead.setScore(maxScore(lead.getScore(), VISIT_INTEREST_SCORE));
        return leadRepository.save(lead);
    }

    private Lead findByIdOrThrow(UUID leadId) {
        return leadRepository.findById(leadId)
                .orElseThrow(() -> new NoSuchElementException("Lead not found: " + leadId));
    }

    private int maxScore(Integer currentScore, int minimumScore) {
        if (currentScore == null) {
            return minimumScore;
        }
        return Math.max(currentScore, minimumScore);
    }
}
