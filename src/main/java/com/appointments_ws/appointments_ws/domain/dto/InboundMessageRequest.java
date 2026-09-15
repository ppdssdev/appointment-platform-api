package com.appointments_ws.appointments_ws.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record InboundMessageRequest(
        @NotBlank(message = "destinationPhoneNumber é obrigatório")
        @Pattern(regexp = "\\d{10,15}", message = "destinationPhoneNumber deve conter entre 10 e 15 dígitos")
        String destinationPhoneNumber,

        @NotBlank(message = "customerPhoneNumber é obrigatório")
        @Pattern(regexp = "\\d{10,15}", message = "customerPhoneNumber deve conter entre 10 e 15 dígitos")
        String customerPhoneNumber,

        @NotBlank(message = "message é obrigatória")
        String message
) {
}