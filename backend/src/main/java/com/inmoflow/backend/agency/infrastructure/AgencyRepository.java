package com.inmoflow.backend.agency.infrastructure;

import com.inmoflow.backend.agency.domain.Agency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AgencyRepository extends JpaRepository<Agency, UUID> {
}