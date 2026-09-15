package com.appointments_ws.appointments_ws.service;

import com.appointments_ws.appointments_ws.domain.dto.AppointmentResponse;
import com.appointments_ws.appointments_ws.domain.dto.RescheduleAppointmentRequest;
import com.appointments_ws.appointments_ws.domain.dto.ScheduleAppointmentRequest;
import com.appointments_ws.appointments_ws.domain.entity.Appointment;
import com.appointments_ws.appointments_ws.domain.entity.Customer;
import com.appointments_ws.appointments_ws.domain.entity.Professional;
import com.appointments_ws.appointments_ws.domain.entity.ProfessionalAvailability;
import com.appointments_ws.appointments_ws.domain.enums.AppointmentStatus;
import com.appointments_ws.appointments_ws.domain.exception.BusinessRuleException;
import com.appointments_ws.appointments_ws.domain.exception.ConflictException;
import com.appointments_ws.appointments_ws.domain.exception.ResourceNotFoundException;
import com.appointments_ws.appointments_ws.repository.AppointmentRepository;
import com.appointments_ws.appointments_ws.repository.CustomerRepository;
import com.appointments_ws.appointments_ws.repository.ProfessionalAvailabilityRepository;
import com.appointments_ws.appointments_ws.repository.ProfessionalRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.UUID;

@Service
public class AppointmentService {
    private static final EnumSet<AppointmentStatus> BLOCKING_STATUSES =
            EnumSet.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);

    private final AppointmentRepository appointments;
    private final CustomerRepository customers;
    private final ProfessionalRepository professionals;
    private final ProfessionalAvailabilityRepository availability;

    public AppointmentService(AppointmentRepository appointments, CustomerRepository customers,
                              ProfessionalRepository professionals, ProfessionalAvailabilityRepository availability) {
        this.appointments = appointments;
        this.customers = customers;
        this.professionals = professionals;
        this.availability = availability;
    }

    @Transactional
    public AppointmentResponse schedule(UUID tenantId, String idempotencyKey, ScheduleAppointmentRequest request) {
        validateTimes(request.startsAt(), request.endsAt());
        Professional professional = lockActiveProfessional(tenantId, request.professionalId());
        Appointment existing = appointments.findByTenantIdAndIdempotencyKey(tenantId, idempotencyKey).orElse(null);
        if (existing != null) {
            if (!sameRequest(existing, request)) {
                throw new ConflictException("Idempotency-Key was already used for a different request");
            }
            return response(existing);
        }
        Customer customer = customers.findByIdAndTenantId(request.customerId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        validateAvailability(professional, request.startsAt(), request.endsAt());
        validateNoOverlap(professional.getId(), request.startsAt(), request.endsAt(), UUID.randomUUID());

        Appointment appointment = new Appointment();
        appointment.setTenant(professional.getTenant());
        appointment.setCustomer(customer);
        appointment.setProfessional(professional);
        appointment.setStartsAt(request.startsAt());
        appointment.setEndsAt(request.endsAt());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setIdempotencyKey(idempotencyKey);
        return save(appointment);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse get(UUID tenantId, UUID id) {
        return response(find(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> list(UUID tenantId, Pageable pageable) {
        return appointments.findByTenantId(tenantId, pageable).map(AppointmentService::response);
    }

    @Transactional
    public AppointmentResponse confirm(UUID tenantId, UUID id) {
        Appointment appointment = find(tenantId, id);
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BusinessRuleException("Only pending appointments can be confirmed");
        }
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return response(appointment);
    }

    @Transactional
    public AppointmentResponse cancel(UUID tenantId, UUID id) {
        Appointment appointment = find(tenantId, id);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return response(appointment);
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return response(appointment);
    }

    @Transactional
    public AppointmentResponse reschedule(UUID tenantId, UUID id, RescheduleAppointmentRequest request) {
        validateTimes(request.startsAt(), request.endsAt());
        Appointment appointment = find(tenantId, id);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessRuleException("Cancelled appointments cannot be rescheduled");
        }
        Professional professional = lockActiveProfessional(tenantId, appointment.getProfessional().getId());
        validateAvailability(professional, request.startsAt(), request.endsAt());
        validateNoOverlap(professional.getId(), request.startsAt(), request.endsAt(), appointment.getId());
        appointment.setStartsAt(request.startsAt());
        appointment.setEndsAt(request.endsAt());
        appointment.setStatus(AppointmentStatus.PENDING);
        return save(appointment);
    }

    private Professional lockActiveProfessional(UUID tenantId, UUID professionalId) {
        Professional professional = professionals.findForUpdateByIdAndTenantId(professionalId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Professional not found"));
        if (!professional.getActive()) {
            throw new BusinessRuleException("Professional is inactive");
        }
        return professional;
    }

    private void validateTimes(Instant startsAt, Instant endsAt) {
        if (!endsAt.isAfter(startsAt)) {
            throw new BusinessRuleException("Appointment end must be after start");
        }
    }

    private void validateAvailability(Professional professional, Instant startsAt, Instant endsAt) {
        ZoneId zone = ZoneId.of(professional.getTimeZone());
        ZonedDateTime localStart = startsAt.atZone(zone);
        ZonedDateTime localEnd = endsAt.atZone(zone);
        if (!localStart.toLocalDate().equals(localEnd.toLocalDate())) {
            throw new BusinessRuleException("Appointment must start and end on the same local day");
        }
        boolean available = availability
                .findByProfessionalIdAndProfessionalTenantIdOrderByDayOfWeekAscStartTimeAsc(
                        professional.getId(), professional.getTenant().getId())
                .stream().filter(window -> window.getDayOfWeek() == localStart.getDayOfWeek())
                .anyMatch(window -> contains(window, localStart, localEnd));
        if (!available) {
            throw new BusinessRuleException("Appointment is outside professional availability");
        }
    }

    private boolean contains(ProfessionalAvailability window, ZonedDateTime start, ZonedDateTime end) {
        return !start.toLocalTime().isBefore(window.getStartTime()) && !end.toLocalTime().isAfter(window.getEndTime());
    }

    private void validateNoOverlap(UUID professionalId, Instant start, Instant end, UUID excludedId) {
        if (appointments.existsByProfessionalIdAndStatusInAndStartsAtLessThanAndEndsAtGreaterThanAndIdNot(
                professionalId, BLOCKING_STATUSES, end, start, excludedId)) {
            throw new ConflictException("Professional already has an appointment in this time range");
        }
    }

    private AppointmentResponse save(Appointment appointment) {
        try {
            return response(appointments.saveAndFlush(appointment));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Professional already has an appointment in this time range");
        }
    }

    private Appointment find(UUID tenantId, UUID id) {
        return appointments.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
    }

    private boolean sameRequest(Appointment appointment, ScheduleAppointmentRequest request) {
        return appointment.getCustomer().getId().equals(request.customerId())
                && appointment.getProfessional().getId().equals(request.professionalId())
                && appointment.getStartsAt().equals(request.startsAt())
                && appointment.getEndsAt().equals(request.endsAt());
    }

    static AppointmentResponse response(Appointment appointment) {
        return new AppointmentResponse(appointment.getId(), appointment.getCustomer().getId(),
                appointment.getProfessional().getId(), appointment.getStartsAt(), appointment.getEndsAt(),
                appointment.getStatus(), appointment.getCreatedAt(), appointment.getUpdatedAt());
    }
}
