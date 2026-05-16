package com.inmoflow.backend.property.infrastructure;

import com.inmoflow.backend.property.domain.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {

    Optional<Property> findByAgencyIdAndReference(UUID agencyId, String reference);
}
