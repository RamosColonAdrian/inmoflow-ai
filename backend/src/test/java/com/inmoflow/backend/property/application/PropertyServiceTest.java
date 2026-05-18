package com.inmoflow.backend.property.application;

import com.inmoflow.backend.property.domain.Property;
import com.inmoflow.backend.property.infrastructure.PropertyRepository;
import com.inmoflow.backend.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    private PropertyService propertyService;

    @BeforeEach
    void setUp() {
        propertyService = new PropertyService(propertyRepository);
    }

    @Test
    void updatesQualificationRulesText() {
        UUID propertyId = UUID.randomUUID();
        Property property = Property.builder()
                .id(propertyId)
                .qualificationRulesText(null)
                .build();
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Property updated = propertyService.updateQualificationRules(
                propertyId,
                "  No mascotas. No estudiantes.  "
        );

        assertThat(updated.getQualificationRulesText()).isEqualTo("No mascotas. No estudiantes.");
        verify(propertyRepository).save(property);
    }

    @Test
    void clearsQualificationRulesTextWhenBlank() {
        UUID propertyId = UUID.randomUUID();
        Property property = Property.builder()
                .id(propertyId)
                .qualificationRulesText("No mascotas.")
                .build();
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Property updated = propertyService.updateQualificationRules(propertyId, "   ");

        assertThat(updated.getQualificationRulesText()).isNull();
    }

    @Test
    void throwsWhenPropertyDoesNotExist() {
        UUID propertyId = UUID.randomUUID();
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.updateQualificationRules(propertyId, "No mascotas."))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Property not found");
    }
}
