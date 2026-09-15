package com.appointments_ws.appointments_ws.controller;

import com.appointments_ws.appointments_ws.config.security.TenantContext;
import com.appointments_ws.appointments_ws.domain.dto.AppointmentResponse;
import com.appointments_ws.appointments_ws.domain.dto.RescheduleAppointmentRequest;
import com.appointments_ws.appointments_ws.domain.dto.ScheduleAppointmentRequest;
import com.appointments_ws.appointments_ws.service.AppointmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {
    private final AppointmentService service;
    private final TenantContext tenantContext;

    public AppointmentController(AppointmentService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AppointmentResponse schedule(@AuthenticationPrincipal Jwt jwt,
                                 @RequestHeader("Idempotency-Key") @NotBlank @Size(max = 100) String idempotencyKey,
                                 @Valid @RequestBody ScheduleAppointmentRequest request) {
        return service.schedule(tenantContext.tenantId(jwt), idempotencyKey, request);
    }

    @GetMapping("/{id}")
    AppointmentResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.get(tenantContext.tenantId(jwt), id);
    }

    @GetMapping
    Page<AppointmentResponse> list(@AuthenticationPrincipal Jwt jwt,
                                   @PageableDefault(size = 20, sort = "startsAt") Pageable pageable) {
        return service.list(tenantContext.tenantId(jwt), pageable);
    }

    @PostMapping("/{id}/confirm")
    AppointmentResponse confirm(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.confirm(tenantContext.tenantId(jwt), id);
    }

    @PostMapping("/{id}/cancel")
    AppointmentResponse cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.cancel(tenantContext.tenantId(jwt), id);
    }

    @PutMapping("/{id}/schedule")
    AppointmentResponse reschedule(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                   @Valid @RequestBody RescheduleAppointmentRequest request) {
        return service.reschedule(tenantContext.tenantId(jwt), id, request);
    }
}
