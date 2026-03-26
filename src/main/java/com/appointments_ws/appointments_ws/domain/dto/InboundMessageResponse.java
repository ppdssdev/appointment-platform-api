package com.appointments_ws.appointments_ws.domain.dto;

import java.util.UUID;

public record InboundMessageResponse(
        UUID tenantId,
        UUID customerId,
        String message,
        String status
) {
}
