package com.appointments_ws.appointments_ws.domain.dto;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(UUID id, String fullName, String phoneNumber, Instant createdAt, Instant updatedAt) {
}
