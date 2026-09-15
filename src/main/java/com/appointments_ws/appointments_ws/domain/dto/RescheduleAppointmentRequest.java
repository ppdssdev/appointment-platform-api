package com.appointments_ws.appointments_ws.domain.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record RescheduleAppointmentRequest(@NotNull @Future Instant startsAt, @NotNull @Future Instant endsAt) {
}
