package com.appointments_ws.appointments_ws.service;

import com.appointments_ws.appointments_ws.domain.dto.ProfessionalRequest;
import com.appointments_ws.appointments_ws.domain.dto.ProfessionalResponse;
import com.appointments_ws.appointments_ws.domain.entity.Professional;
import com.appointments_ws.appointments_ws.domain.entity.Tenant;
import com.appointments_ws.appointments_ws.domain.exception.ResourceNotFoundException;
import com.appointments_ws.appointments_ws.repository.ProfessionalRepository;
import com.appointments_ws.appointments_ws.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.time.DateTimeException;
import java.time.ZoneId;
import com.appointments_ws.appointments_ws.domain.exception.BusinessRuleException;

@Service
public class ProfessionalService {
    private final ProfessionalRepository professionals;
    private final TenantRepository tenants;

    public ProfessionalService(ProfessionalRepository professionals, TenantRepository tenants) {
        this.professionals = professionals;
        this.tenants = tenants;
    }

    @Transactional
    public ProfessionalResponse create(UUID tenantId, ProfessionalRequest request) {
        Tenant tenant = tenants.findById(tenantId).filter(Tenant::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Active tenant not found"));
        Professional professional = new Professional();
        professional.setTenant(tenant);
        apply(professional, request);
        return response(professionals.save(professional));
    }

    @Transactional
    public ProfessionalResponse update(UUID tenantId, UUID id, ProfessionalRequest request) {
        Professional professional = professionals.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found"));
        apply(professional, request);
        return response(professional);
    }

    private void apply(Professional professional, ProfessionalRequest request) {
        professional.setFullName(request.fullName().trim());
        professional.setActive(request.active());
        try {
            professional.setTimeZone(ZoneId.of(request.timeZone()).getId());
        } catch (DateTimeException exception) {
            throw new BusinessRuleException("Professional timeZone must be a valid IANA zone ID");
        }
    }

    static ProfessionalResponse response(Professional professional) {
        return new ProfessionalResponse(professional.getId(), professional.getFullName(), professional.getActive(), professional.getTimeZone(),
                professional.getCreatedAt(), professional.getUpdatedAt());
    }
}
