package com.appointments_ws.appointments_ws.repository;

import com.appointments_ws.appointments_ws.domain.entity.Appointment;
import com.appointments_ws.appointments_ws.domain.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    @EntityGraph(attributePaths = {"customer", "professional"})
    Optional<Appointment> findByIdAndTenantId(UUID id, UUID tenantId);

    @EntityGraph(attributePaths = {"customer", "professional"})
    Page<Appointment> findByTenantId(UUID tenantId, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "professional"})
    Optional<Appointment> findByTenantIdAndIdempotencyKey(UUID tenantId, String idempotencyKey);

    boolean existsByProfessionalIdAndStatusInAndStartsAtLessThanAndEndsAtGreaterThanAndIdNot(
            UUID professionalId, Collection<AppointmentStatus> statuses, Instant end, Instant start, UUID excludedId);
}
