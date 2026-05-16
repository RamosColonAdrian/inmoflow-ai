package com.inmoflow.backend.lead.infrastructure;

import com.inmoflow.backend.lead.domain.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {

    List<Lead> findByAgencyId(UUID agencyId);
}
