package com.inmoflow.backend.property.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @Id
    private UUID id;

    @Column(name = "agency_id", nullable = false)
    private UUID agencyId;

    @Column(nullable = false, length = 80)
    private String reference;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(length = 120)
    private String city;

    @Column(length = 120)
    private String zone;

    @Column()
    private String address;

    private Integer rooms;

    private Integer bathrooms;

    @Column(name = "square_meters")
    private Integer squareMeters;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false, length = 50)
    private PropertyType propertyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 50)
    private OperationType operationType;

    @Column(nullable = false)
    private Boolean available;

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    @Column(name = "qualification_rules_text", columnDefinition = "TEXT")
    private String qualificationRulesText;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (id == null) {
            id = UUID.randomUUID();
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
