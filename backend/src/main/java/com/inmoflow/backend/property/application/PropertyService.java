package com.inmoflow.backend.property.application;

import com.inmoflow.backend.property.domain.Property;
import com.inmoflow.backend.property.infrastructure.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .build();

        return propertyRepository.save(property);
    }

    @Transactional(readOnly = true)
    public Page<Property> findAll(Pageable pageable) {
        return propertyRepository.findAll(pageable);
    }
}
