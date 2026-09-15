package com.appointments_ws.appointments_ws.controller;

import com.appointments_ws.appointments_ws.config.security.TenantContext;
import com.appointments_ws.appointments_ws.domain.dto.CustomerRequest;
import com.appointments_ws.appointments_ws.domain.dto.CustomerResponse;
import com.appointments_ws.appointments_ws.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService service;
    private final TenantContext tenantContext;

    public CustomerController(CustomerService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CustomerResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CustomerRequest request) {
        return service.create(tenantContext.tenantId(jwt), request);
    }

    @PutMapping("/{id}")
    CustomerResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                            @Valid @RequestBody CustomerRequest request) {
        return service.update(tenantContext.tenantId(jwt), id, request);
    }
}
