package com.appointments_ws.appointments_ws.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record InboundMessageRequest(
        @NotBlank String destinationPhoneNumber,
        @NotBlank String customerPhoneNumber,
        @NotBlank String message
) {
}