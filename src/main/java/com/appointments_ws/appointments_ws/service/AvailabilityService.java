package com.appointments_ws.appointments_ws.service;

import com.appointments_ws.appointments_ws.domain.dto.AvailabilityRequest;
import com.appointments_ws.appointments_ws.domain.dto.AvailabilityResponse;
import com.appointments_ws.appointments_ws.domain.entity.Professional;
import com.appointments_ws.appointments_ws.domain.entity.ProfessionalAvailability;
import com.appointments_ws.appointments_ws.domain.exception.BusinessRuleException;
import com.appointments_ws.appointments_ws.domain.exception.ConflictException;
import com.appointments_ws.appointments_ws.domain.exception.ResourceNotFoundException;
import com.appointments_ws.appointments_ws.repository.ProfessionalAvailabilityRepository;
import com.appointments_ws.appointments_ws.repository.ProfessionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AvailabilityService {
    private final ProfessionalRepository professionals;
    private final ProfessionalAvailabilityRepository availability;

    public AvailabilityService(ProfessionalRepository professionals, ProfessionalAvailabilityRepository availability) {
        this.professionals = professionals;
        this.availability = availability;
    }

    @Transactional
    public AvailabilityResponse define(UUID tenantId, UUID professionalId, AvailabilityRequest request) {
        Professional professional = professionals.findByIdAndTenantId(professionalId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found"));
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BusinessRuleException("Availability end time must be after start time");
        }
        if (availability.existsByProfessionalIdAndDayOfWeekAndStartTimeLessThanAndEndTimeGreaterThan(
                professionalId, request.dayOfWeek(), request.endTime(), request.startTime())) {
            throw new ConflictException("Availability overlaps an existing time window");
        }
        ProfessionalAvailability window = new ProfessionalAvailability();
        window.setProfessional(professional);
        window.setDayOfWeek(request.dayOfWeek());
        window.setStartTime(request.startTime());
        window.setEndTime(request.endTime());
        return response(availability.save(window));
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> list(UUID tenantId, UUID professionalId) {
        professionals.findByIdAndTenantId(professionalId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found"));
        return availability.findByProfessionalIdAndProfessionalTenantIdOrderByDayOfWeekAscStartTimeAsc(professionalId, tenantId)
                .stream().map(AvailabilityService::response).toList();
    }

    private static AvailabilityResponse response(ProfessionalAvailability value) {
        return new AvailabilityResponse(value.getId(), value.getDayOfWeek(), value.getStartTime(), value.getEndTime());
    }
}
