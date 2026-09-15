package com.appointments_ws.appointments_ws.controller;

import com.appointments_ws.appointments_ws.config.security.TenantContext;
import com.appointments_ws.appointments_ws.domain.dto.ProfessionalRequest;
import com.appointments_ws.appointments_ws.domain.dto.ProfessionalResponse;
import com.appointments_ws.appointments_ws.domain.dto.AvailabilityRequest;
import com.appointments_ws.appointments_ws.domain.dto.AvailabilityResponse;
import com.appointments_ws.appointments_ws.service.AvailabilityService;
import com.appointments_ws.appointments_ws.service.ProfessionalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/professionals")
public class ProfessionalController {
    private final ProfessionalService service;
    private final TenantContext tenantContext;
    private final AvailabilityService availabilityService;

    public ProfessionalController(ProfessionalService service, TenantContext tenantContext, AvailabilityService availabilityService) {
        this.service = service;
        this.tenantContext = tenantContext;
        this.availabilityService = availabilityService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ProfessionalResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ProfessionalRequest request) {
        return service.create(tenantContext.tenantId(jwt), request);
    }

    @PutMapping("/{id}")
    ProfessionalResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                @Valid @RequestBody ProfessionalRequest request) {
        return service.update(tenantContext.tenantId(jwt), id, request);
    }

    @PostMapping("/{id}/availability")
    @ResponseStatus(HttpStatus.CREATED)
    AvailabilityResponse defineAvailability(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                            @Valid @RequestBody AvailabilityRequest request) {
        return availabilityService.define(tenantContext.tenantId(jwt), id, request);
    }

    @GetMapping("/{id}/availability")
    List<AvailabilityResponse> listAvailability(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return availabilityService.list(tenantContext.tenantId(jwt), id);
    }
}
