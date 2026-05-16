package com.inmoflow.backend.property.api;

import com.inmoflow.backend.property.api.request.CreatePropertyRequest;
import com.inmoflow.backend.property.api.response.PropertyResponse;
import com.inmoflow.backend.property.application.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyResponse create(@Valid @RequestBody CreatePropertyRequest request) {
        return PropertyResponse.from(propertyService.create(request));
    }

    @GetMapping
    public List<PropertyResponse> findAll() {
        return propertyService.findAll()
                .stream()
                .map(PropertyResponse::from)
                .toList();
    }
}
