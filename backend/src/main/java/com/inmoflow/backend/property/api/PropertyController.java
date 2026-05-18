package com.inmoflow.backend.property.api;

import com.inmoflow.backend.property.api.request.CreatePropertyRequest;
import com.inmoflow.backend.property.api.request.UpdateQualificationRulesRequest;
import com.inmoflow.backend.property.api.response.PropertyResponse;
import com.inmoflow.backend.property.application.CreatePropertyCommand;
import com.inmoflow.backend.property.application.PropertyService;
import com.inmoflow.backend.shared.api.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private static final int MAX_PAGE_SIZE = 100;

    private final PropertyService propertyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyResponse create(@Valid @RequestBody CreatePropertyRequest request) {
        CreatePropertyCommand command = new CreatePropertyCommand(
                request.agencyId(),
                request.reference(),
                request.title(),
                request.description(),
                request.price(),
                request.city(),
                request.zone(),
                request.address(),
                request.rooms(),
                request.bathrooms(),
                request.squareMeters(),
                request.propertyType(),
                request.operationType(),
                request.available(),
                request.sourceUrl(),
                request.qualificationRulesText()
        );

        return PropertyResponse.from(propertyService.create(command));
    }

    @GetMapping
    public PageResponse<PropertyResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return PageResponse.from(propertyService.findAll(pageRequest(page, size))
                .map(PropertyResponse::from));
    }

    @PatchMapping("/{propertyId}/qualification-rules")
    public PropertyResponse updateQualificationRules(
            @PathVariable UUID propertyId,
            @Valid @RequestBody UpdateQualificationRulesRequest request
    ) {
        return PropertyResponse.from(propertyService.updateQualificationRules(
                propertyId,
                request.qualificationRulesText()
        ));
    }

    private PageRequest pageRequest(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize);
    }
}
