package com.inmoflow.backend.lead.application;

import com.inmoflow.backend.lead.domain.Lead;
import com.inmoflow.backend.lead.domain.LeadStatus;
import com.inmoflow.backend.lead.infrastructure.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;

    private LeadService leadService;

    @BeforeEach
    void setUp() {
        leadService = new LeadService(leadRepository);
    }

    @Test
    void qualifiesLeadForVisitRequestWithMinimumScore() {
        UUID leadId = UUID.randomUUID();
        Lead lead = Lead.builder()
                .id(leadId)
                .status(LeadStatus.NEW)
                .score(60)
                .build();
        when(leadRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Lead updated = leadService.qualifyForVisitRequest(leadId);

        assertThat(updated.getStatus()).isEqualTo(LeadStatus.QUALIFIED);
        assertThat(updated.getScore()).isEqualTo(90);
    }

    @Test
    void qualifiesLeadWithoutLoweringHigherScore() {
        UUID leadId = UUID.randomUUID();
        Lead lead = Lead.builder()
                .id(leadId)
                .status(LeadStatus.QUALIFIED)
                .score(95)
                .build();
        when(leadRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Lead updated = leadService.qualifyForVisitRequest(leadId);

        assertThat(updated.getStatus()).isEqualTo(LeadStatus.QUALIFIED);
        assertThat(updated.getScore()).isEqualTo(95);
    }

    @Test
    void marksNewLeadAsContactedForVisitInterestWithMinimumScore() {
        UUID leadId = UUID.randomUUID();
        Lead lead = Lead.builder()
                .id(leadId)
                .status(LeadStatus.NEW)
                .score(40)
                .build();
        when(leadRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Lead updated = leadService.markContactedForVisitInterest(leadId);

        assertThat(updated.getStatus()).isEqualTo(LeadStatus.CONTACTED);
        assertThat(updated.getScore()).isEqualTo(70);
    }

    @Test
    void marksVisitInterestWithoutDowngradingExistingStatusOrScore() {
        UUID leadId = UUID.randomUUID();
        Lead lead = Lead.builder()
                .id(leadId)
                .status(LeadStatus.QUALIFIED)
                .score(95)
                .build();
        when(leadRepository.findById(leadId)).thenReturn(Optional.of(lead));
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Lead updated = leadService.markContactedForVisitInterest(leadId);

        assertThat(updated.getStatus()).isEqualTo(LeadStatus.QUALIFIED);
        assertThat(updated.getScore()).isEqualTo(95);
    }
}
