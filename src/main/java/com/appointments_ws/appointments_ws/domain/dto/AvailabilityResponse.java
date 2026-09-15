package com.appointments_ws.appointments_ws.domain.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record AvailabilityResponse(UUID id, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
}
