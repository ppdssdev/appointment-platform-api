package com.appointments_ws.appointments_ws.domain.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record ScheduleAppointmentRequest(@NotNull UUID customerId, @NotNull UUID professionalId,
                                         @NotNull @Future Instant startsAt, @NotNull @Future Instant endsAt) {
}
