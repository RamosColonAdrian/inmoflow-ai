package com.inmoflow.backend.property.application;

import com.inmoflow.backend.property.api.request.CreatePropertyRequest;
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
    public Property create(CreatePropertyRequest request) {
        Property property = Property.builder()
                .agencyId(request.agencyId())
                .reference(request.reference())
                .title(request.title())
                .description(request.description())
                .price(request.price())
                .city(request.city())
                .zone(request.zone())
                .address(request.address())
                .rooms(request.rooms())
                .bathrooms(request.bathrooms())
                .squareMeters(request.squareMeters())
                .propertyType(request.propertyType())
                .operationType(request.operationType())
                .available(request.available())
                .build();

        return propertyRepository.save(property);
    }

    @Transactional(readOnly = true)
    public Page<Property> findAll(Pageable pageable) {
        return propertyRepository.findAll(pageable);
    }
}
