package com.appointments_ws.appointments_ws.domain.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        List<String> details
) {
}