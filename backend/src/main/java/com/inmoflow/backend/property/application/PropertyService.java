package com.inmoflow.backend.property.application;

import com.inmoflow.backend.property.domain.Property;
import com.inmoflow.backend.property.infrastructure.PropertyRepository;
import com.inmoflow.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;

    @Transactional
    public Property create(CreatePropertyCommand command) {
        Property property = Property.builder()
                .agencyId(command.agencyId())
                .reference(command.reference())
                .title(command.title())
                .description(command.description())
                .price(command.price())
                .city(command.city())
                .zone(command.zone())
                .address(command.address())
                .rooms(command.rooms())
                .bathrooms(command.bathrooms())
                .squareMeters(command.squareMeters())
                .propertyType(command.propertyType())
                .operationType(command.operationType())
                .available(command.available())
                .sourceUrl(command.sourceUrl())
                .qualificationRulesText(command.qualificationRulesText())
                .build();

        return propertyRepository.save(property);
    }

    @Transactional(readOnly = true)
    public Page<Property> findAll(Pageable pageable) {
        return propertyRepository.findAll(pageable);
    }

    @Transactional
    public Property updateQualificationRules(UUID propertyId, String qualificationRulesText) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));

        property.setQualificationRulesText(normalizeNullableText(qualificationRulesText));
        return propertyRepository.save(property);
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
