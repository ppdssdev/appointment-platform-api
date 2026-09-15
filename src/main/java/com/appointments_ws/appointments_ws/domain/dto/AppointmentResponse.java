package com.appointments_ws.appointments_ws.domain.dto;

import com.appointments_ws.appointments_ws.domain.enums.AppointmentStatus;

import java.time.Instant;
import java.util.UUID;

public record AppointmentResponse(UUID id, UUID customerId, UUID professionalId, Instant startsAt, Instant endsAt,
                                  AppointmentStatus status, Instant createdAt, Instant updatedAt) {
}
