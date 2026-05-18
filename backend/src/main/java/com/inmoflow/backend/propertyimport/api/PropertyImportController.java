package com.inmoflow.backend.propertyimport.api;

import com.inmoflow.backend.propertyimport.application.PropertyImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/property-imports")
@RequiredArgsConstructor
public class PropertyImportController {

    private final PropertyImportService propertyImportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ImportPropertiesResponse importProperties(@Valid @RequestBody ImportPropertiesRequest request) {
        return propertyImportService.importProperties(request);
    }
}
