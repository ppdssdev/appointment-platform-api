package com.appointments_ws.appointments_ws.domain.dto;

import java.time.Instant;
import java.util.UUID;

public record ProfessionalResponse(UUID id, String fullName, boolean active, String timeZone,
                                   Instant createdAt, Instant updatedAt) {
}
