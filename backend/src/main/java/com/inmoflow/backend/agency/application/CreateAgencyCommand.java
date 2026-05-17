package com.inmoflow.backend.agency.application;

public record CreateAgencyCommand(
        String name,
        String email,
        String phone,
        String website
) {
}
