package com.inmoflow.backend.agency.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAgencyRequest(

        @NotBlank(message = "Agency name is required")
        @Size(max = 150, message = "Agency name must not exceed 150 characters")
        String name,

        @Email(message = "Email must be valid")
        @Size(max = 180, message = "Email must not exceed 180 characters")
        String email,

        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone,

        @Size(max = 255, message = "Website must not exceed 255 characters")
        String website
) {
}