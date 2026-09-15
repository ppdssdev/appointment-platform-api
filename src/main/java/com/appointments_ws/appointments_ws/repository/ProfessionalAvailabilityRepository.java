package com.appointments_ws.appointments_ws.repository;

import com.appointments_ws.appointments_ws.domain.entity.ProfessionalAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface ProfessionalAvailabilityRepository extends JpaRepository<ProfessionalAvailability, UUID> {
    List<ProfessionalAvailability> findByProfessionalIdAndProfessionalTenantIdOrderByDayOfWeekAscStartTimeAsc(
            UUID professionalId, UUID tenantId);

    boolean existsByProfessionalIdAndDayOfWeekAndStartTimeLessThanAndEndTimeGreaterThan(
            UUID professionalId, DayOfWeek dayOfWeek, LocalTime endTime, LocalTime startTime);
}
