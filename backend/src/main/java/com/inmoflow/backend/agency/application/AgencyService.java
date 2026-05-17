package com.inmoflow.backend.agency.application;

import com.inmoflow.backend.agency.domain.Agency;
import com.inmoflow.backend.agency.infrastructure.AgencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgencyService {

    private final AgencyRepository agencyRepository;

    @Transactional
    public Agency create(CreateAgencyCommand command) {
        Agency agency = Agency.builder()
                .name(command.name())
                .email(command.email())
                .phone(command.phone())
                .website(command.website())
                .build();

        return agencyRepository.save(agency);
    }

    @Transactional(readOnly = true)
    public Page<Agency> findAll(Pageable pageable) {
        return agencyRepository.findAll(pageable);
    }
}
